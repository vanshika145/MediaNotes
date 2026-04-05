# 🚀 QUICK START GUIDE FOR DEVELOPERS

## 5-MINUTE SUMMARY

### What Changed?

#### Database ✓
```
NotesDB_29  →  NotesDB_52
notes_29    →  notes_52
priority    →  note_type
```

#### UI ✓
```
Priority field label  →  Note Type
Hint text updated
Modern Material Design 3 applied
```

#### Backend ✓
```
Note.getPriority()  →  Note.getNoteType()
All queries updated
Notification message updated
```

---

## 🔨 DEVELOPER CHECKLIST

### To Continue Development:

```
1. Pull latest code
   □ Check these files are updated:
     - DBHelper.java
     - Note.java
     - MainActivity.java
     - NotesAdapter.java
     - activity_main.xml
     - NotificationWorker.java

2. Verify Database
   □ Database: NotesDB_52
   □ Table: notes_52
   □ Column: note_type (not priority)

3. Test Locally
   □ Create a test note
   □ Save it with Note Type="Test"
   □ View it in RecyclerView
   □ Check notifications work

4. Make New Features
   □ Use noteType variable (not priority)
   □ Use getNoteType() getter (not getPriority())
   □ Follow Material Design 3 color scheme
   □ Use new spacing system (dimens.xml)
```

---

## 🎨 DESIGN SYSTEM

### Colors (Use These)
```java
// Primary actions
R.color.primary           // #6366F1 - Main color
R.color.primary_dark      // #4F46E5 - Pressed state

// Accents
R.color.secondary         // #06B6D4 - Secondary actions
R.color.tertiary          // #EC4899 - Highlights
R.color.accent            // #F59E0B - Warnings

// Text
R.color.black             // #1F2937 - Main text
R.color.gray_600          // #4B5563 - Body text
R.color.gray_500          // #6B7280 - Secondary text
R.color.gray_400          // #9CA3AF - Hints

// Status
R.color.success           // #10B981 - Success
R.color.error             // #EF4444 - Errors
R.color.warning           // #F59E0B - Warnings
```

### Typography
```xml
<TextView
    android:textSize="@dimen/text_size_display"
    android:textColor="@color/black"
    android:textStyle="bold" />
```

Available sizes:
- text_size_display (32sp)
- text_size_headline (28sp)
- text_size_title (18sp)
- text_size_body (14sp)
- text_size_label (12sp)
- text_size_caption (11sp)

### Spacing
```xml
android:layout_margin="@dimen/spacing_lg"      // 16dp
android:padding="@dimen/spacing_xl"            // 20dp
android:layout_marginTop="@dimen/spacing_md"   // 12dp
```

Available spacing:
- spacing_xs (4dp)
- spacing_sm (8dp)
- spacing_md (12dp)
- spacing_lg (16dp)
- spacing_xl (20dp)
- spacing_xxl (24dp)

---

## 📱 DATABASE QUICK REFERENCE

### Insert Note
```java
dbHelper.insertNote(
    "My Title",                    // title
    "My description",              // description
    "/path/to/image",              // imagePath
    "2026-04-05 14:30:00",        // date
    "Work"                         // noteType (was priority)
);
```

### Query Notes
```java
Cursor cursor = dbHelper.getNotes();

while (cursor.moveToNext()) {
    String title = cursor.getString(
        cursor.getColumnIndex(DBHelper.COLUMN_TITLE)
    );
    String noteType = cursor.getString(
        cursor.getColumnIndex(DBHelper.COLUMN_NOTE_TYPE)
    );
    // Use the data...
}
```

### Create Note Object
```java
Note note = new Note(
    1,                             // id
    "My Title",                    // title
    "Description",                 // description
    "/path/to/image",              // imagePath
    "2026-04-05 14:30:00",        // date
    "Work"                         // noteType (was priority)
);

// Access fields
String type = note.getNoteType();  // "Work"
```

---

## 🔄 COMMON TASKS

### Task 1: Add New Field to Notes

```java
// 1. Update database schema (in DBHelper)
public static final String COLUMN_TAGS = "tags";

// 2. Update CREATE TABLE
String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
    // ... existing fields ...
    COLUMN_NOTE_TYPE + " TEXT, " +
    COLUMN_TAGS + " TEXT)";    // ← Add new field

// 3. Update insert method
public long insertNote(String title, String description, 
                       String imagePath, String date, 
                       String noteType, String tags) {  // ← Add parameter
    values.put(COLUMN_TAGS, tags);  // ← Add to ContentValues
}

// 4. Update Note.java
private String tags;
public Note(..., String noteType, String tags) {
    this.tags = tags;
}
public String getTags() { return tags; }

// 5. Update MainActivity.java
String tags = tagsEt.getText().toString();
dbHelper.insertNote(..., noteType, tags);

// 6. Update NotesAdapter.java
holder.tagsTv.setText(note.getTags());
```

### Task 2: Filter Notes by Type

```java
public List<Note> getNotesByType(String type) {
    List<Note> notes = new ArrayList<>();
    
    Cursor cursor = mDatabase.rawQuery(
        "SELECT * FROM " + DBHelper.TABLE_NAME + 
        " WHERE " + DBHelper.COLUMN_NOTE_TYPE + " = ?",
        new String[]{type}
    );
    
    while (cursor.moveToNext()) {
        Note note = new Note(
            cursor.getInt(0),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getString(3),
            cursor.getString(4),
            cursor.getString(5)
        );
        notes.add(note);
    }
    
    cursor.close();
    return notes;
}
```

### Task 3: Implement Search

```java
public List<Note> searchNotes(String query) {
    List<Note> results = new ArrayList<>();
    
    Cursor cursor = mDatabase.rawQuery(
        "SELECT * FROM " + DBHelper.TABLE_NAME + 
        " WHERE " + DBHelper.COLUMN_TITLE + " LIKE ? " +
        " OR " + DBHelper.COLUMN_DESCRIPTION + " LIKE ? " +
        " OR " + DBHelper.COLUMN_NOTE_TYPE + " LIKE ?",
        new String[]{"%" + query + "%", 
                     "%" + query + "%",
                     "%" + query + "%"}
    );
    
    while (cursor.moveToNext()) {
        results.add(createNoteFromCursor(cursor));
    }
    
    cursor.close();
    return results;
}
```

---

## 🧪 TESTING LOCALLY

### Unit Test Example
```java
public class NoteTest {
    
    @Test
    public void testNoteCreation() {
        Note note = new Note(
            1, "Title", "Desc", "/path", "date", "Work"
        );
        
        assertEquals("Work", note.getNoteType());
        assertNotNull(note.getTitle());
    }
    
    @Test
    public void testDatabaseInsert() {
        DBHelper db = new DBHelper(context);
        long id = db.insertNote("T", "D", "/p", "d", "Work");
        
        assertTrue(id > 0);
    }
}
```

### Manual Test Checklist
```
□ Create note with type "Work"
□ Create note with type "Personal"
□ Create note with type "Study"
□ View all notes
□ Verify all types display
□ Create note with image
□ Verify image shows
□ Delete a note
□ Verify deletion in RecyclerView
□ Restart app
□ Verify notes persist
□ Wait for notification (15min)
```

---

## 🐛 DEBUGGING

### Check Database
```bash
adb shell
cd /data/data/com.example.medianotesapp/databases/
sqlite3 NotesDB_52
sqlite> .tables
sqlite> .schema notes_52
sqlite> SELECT * FROM notes_52;
```

### View Logs
```bash
adb logcat | grep medianotesapp
```

### Common Issues

**Issue:** "Column 'priority' not found"
```
Solution: Update code to use COLUMN_NOTE_TYPE
Check: DBHelper.java, Note.java, MainActivity.java
```

**Issue:** "Database 'NotesDB_29' not found"
```
Solution: First run creates NotesDB_52 automatically
Check: DatabaseInspector to confirm
```

**Issue:** Notes not showing in RecyclerView
```
Solution: Verify getNoteType() is being called
Check: NotesAdapter.onBindViewHolder()
```

**Issue:** Notification not showing
```
Solution: Check POST_NOTIFICATIONS permission
Check: AndroidManifest.xml, MainActivity notification permission request
```

---

## 📚 FILE REFERENCE

### To Modify Database
→ Edit: `DBHelper.java`

### To Modify Data Model
→ Edit: `Note.java`

### To Modify UI
→ Edit: `activity_main.xml` or `note_item.xml`

### To Modify Colors
→ Edit: `values/colors.xml`

### To Modify Spacing/Fonts
→ Edit: `values/dimens.xml`

### To Modify Button Styles
→ Edit: `drawable/btn_*.xml`

### To Modify Notification
→ Edit: `NotificationWorker.java`

---

## ✅ CODE STYLE GUIDE

### Naming Convention
```
Database constants:     COLUMN_NOTE_TYPE
Private variables:      private String noteType
Public methods:         public String getNoteType()
Layout IDs:            android:id="@+id/noteType"
Resources:             R.color.primary
```

### Spacing
```
Most fields:   spacing_lg (16dp)
Small gaps:    spacing_md (12dp)
Large sections: spacing_xl (20dp)
```

### Colors
```
Main text:     R.color.black
Secondary:     R.color.gray_600
Hint:          R.color.gray_400
Buttons:       R.color.primary
```

---

## 🎯 DEVELOPMENT TIPS

1. **Always use the new spacing constants**
   - Don't hardcode dp values
   - Reference dimens.xml

2. **Follow Material Design 3**
   - Use provided colors
   - Use provided typography sizes
   - Use rounded corners (12-16dp)

3. **Keep database simple**
   - No complex queries
   - Use helper methods
   - Document any raw SQL

4. **Test on multiple devices**
   - Small phone (4.5")
   - Large phone (6.5")
   - Tablet (10")

5. **Test different note types**
   - User will enter various types
   - Don't assume only specific values

---

## 📞 QUICK REFERENCE

| Need | File | Line |
|------|------|------|
| Database queries | DBHelper.java | 47-62 |
| Note model | Note.java | 10 |
| UI inputs | MainActivity.java | 80-84 |
| Display data | NotesAdapter.java | 34-35 |
| Layout | activity_main.xml | 72-80 |
| Colors | colors.xml | 2-9 |
| Spacing | dimens.xml | 2-10 |

---

## 🚀 YOU'RE READY!

Everything is set up for development. Just:
1. Update any code to use noteType (not priority)
2. Use Material Design 3 colors and spacing
3. Reference the documentation files as needed
4. Test thoroughly

Happy coding! 🎉


