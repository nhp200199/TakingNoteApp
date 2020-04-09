package com.example.notekeeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String NOTE_INFO  = "com.example.notekeeper.NOTE_INFO";
    private NoteInfo mNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spCourses = findViewById(R.id.sp_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spCourses.setAdapter(adapterCourses);

        readDisplayStateValues();

        EditText edtNoteTitle = findViewById(R.id.edt_title);
        EditText edtNoteText = findViewById(R.id.edt_note);

        displayNote(spCourses, edtNoteTitle, edtNoteText);
    }

    private void displayNote(Spinner spCourses, EditText edtNoteTitle, EditText edtNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex  =courses.indexOf(mNote.getCourse());
        spCourses.setSelection(courseIndex);
        edtNoteTitle.setText(mNote.getTitle());
         edtNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNote = intent.getParcelableExtra(MainActivity.NOTE_INFO);
    }
}
