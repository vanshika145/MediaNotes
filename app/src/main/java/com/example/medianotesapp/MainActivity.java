package com.example.medianotesapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // UI Components
    private EditText titleEt, descEt, noteTypeEt;
    private Button selectImageBtn, saveBtn, viewBtn;
    private ImageView imagePreview;
    private TextView noImageText;

    // Database and WorkManager
    private DBHelper dbHelper;
    private String imagePath = "";

    // Sensor Variables (Step 8)
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f; // Threshold for shake detection

    // Constants
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize UI Components
        initViews();

        dbHelper = new DBHelper(this);

        // 2. Initialize Sensors (Step 8)
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // 3. Setup WorkManager & Permissions
        scheduleNotifications();
        requestNotificationPermission();

        // 4. Setup Click Listeners
        setupClickListeners();
    }

    private void initViews() {
        titleEt = findViewById(R.id.title);
        descEt = findViewById(R.id.desc);
        noteTypeEt = findViewById(R.id.priority);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        saveBtn = findViewById(R.id.saveBtn);
        viewBtn = findViewById(R.id.viewBtn);
        imagePreview = findViewById(R.id.imagePreview);
        noImageText = findViewById(R.id.noImageText);
    }

    private void setupClickListeners() {
        // Step 9: Image Selection
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPickImage();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewNotesActivity.class);
                startActivity(intent);
            }
        });
    }


    // ====================================
    // STEP 8: ACCELEROMETER SENSOR
    // ====================================

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listener
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listener to save battery and avoid memory leaks
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate magnitude of acceleration
            // Simple logic: check if any axis exceeds threshold or calculate total movement
            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > SHAKE_THRESHOLD) {
                // Shake detected
                Toast.makeText(this, "Device motion detected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    // ====================================
    // STEP 9: IMAGE SELECTION & PERMISSIONS
    // ====================================

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        }
    }

    private void pickImageFromGallery() {
        // Using Intent.ACTION_PICK to allow user to select image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Store URI as string
                imagePath = selectedImageUri.toString();

                // Enhancement: Display preview in ImageView
                imagePreview.setImageURI(selectedImageUri);
                imagePreview.setAlpha(1.0f);
                noImageText.setVisibility(View.GONE);

                // Persist permission for the URI so it can be read later (in RecyclerView)
                try {
                    final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
                } catch (SecurityException e) {
                    // Fallback or ignore if not supported by the provider
                }
            }
        }
    }

    // ====================================
    // OTHER LOGIC (DB, WORKMANAGER, NOTIF)
    // ====================================

    private void saveNote() {
        String title = titleEt.getText().toString().trim();
        String desc = descEt.getText().toString().trim();
        String noteType = noteTypeEt.getText().toString().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (title.isEmpty() || desc.isEmpty() || noteType.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = dbHelper.insertNote(title, desc, imagePath, date, noteType);

        if (id > 0) {
            Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        titleEt.setText("");
        descEt.setText("");
        noteTypeEt.setText("");
        imagePath = "";
        imagePreview.setImageDrawable(null);
        imagePreview.setAlpha(0.3f);
        noImageText.setVisibility(View.VISIBLE);
    }

    private void scheduleNotifications() {
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, 15, TimeUnit.MINUTES).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NotesNotificationWork", ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
