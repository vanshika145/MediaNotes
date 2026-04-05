package com.example.medianotesapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private Context context;
    private ArrayList<Note> notesList;

    public NotesAdapter(Context context, ArrayList<Note> notesList) {
        this.context = context;
        this.notesList = notesList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notesList.get(position);
        holder.titleTv.setText(note.getTitle());
        holder.descTv.setText(note.getDescription());
        holder.priorityTv.setText(note.getNoteType());
        holder.dateTv.setText(note.getDate());

        if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
            holder.imageView.setImageURI(Uri.parse(note.getImagePath()));
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv, descTv, priorityTv, dateTv;
        ImageView imageView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.noteTitle);
            descTv = itemView.findViewById(R.id.noteDesc);
            priorityTv = itemView.findViewById(R.id.notePriority);
            dateTv = itemView.findViewById(R.id.noteDate);
            imageView = itemView.findViewById(R.id.noteImage);
        }
    }
}
