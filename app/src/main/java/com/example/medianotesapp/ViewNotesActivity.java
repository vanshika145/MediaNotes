package com.example.medianotesapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViewNotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private ArrayList<Note> notesList;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        notesList = new ArrayList<>();

        loadNotes();
    }

    private void loadNotes() {
        Cursor cursor = dbHelper.getNotes();

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No Notes Found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String desc = cursor.getString(2);
                String imagePath = cursor.getString(3);
                String date = cursor.getString(4);
                String priority = cursor.getString(5);

                notesList.add(new Note(id, title, desc, imagePath, date, priority));
            }
            
            adapter = new NotesAdapter(this, notesList);
            recyclerView.setAdapter(adapter);
        }
        cursor.close();
    }
}
