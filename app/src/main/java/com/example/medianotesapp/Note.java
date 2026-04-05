package com.example.medianotesapp;

public class Note {
    private int id;
    private String title;
    private String description;
    private String imagePath;
    private String date;
    private String noteType;

    public Note(int id, String title, String description, String imagePath, String date, String noteType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.date = date;
        this.noteType = noteType;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public String getDate() { return date; }
    public String getNoteType() { return noteType; }
}
