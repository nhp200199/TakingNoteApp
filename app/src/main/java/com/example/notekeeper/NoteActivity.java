package com.example.notekeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.example.notekeeper.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private Spinner mSpCourses;
    private EditText mEdtNoteTitle;
    private EditText mEdtNoteText;
    private boolean mIsNewNote;
    private int mNotePosition;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(savedInstanceState!=null && mViewModel.isNewlyCreated){
            mViewModel.restoreState(savedInstanceState);
        }
        mViewModel.isNewlyCreated =false;

        mSpCourses = findViewById(R.id.sp_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpCourses.setAdapter(adapterCourses);

        readDisplayStateValues();
        saveOriginalNoteValues();

        mEdtNoteTitle = findViewById(R.id.edt_title);
        mEdtNoteText = findViewById(R.id.edt_note);

        if(!mIsNewNote){
            displayNote(mSpCourses, mEdtNoteTitle, mEdtNoteText);
        }

    }

    private void saveOriginalNoteValues() {
        if(mIsNewNote)
            return;
        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNotePosition < lastIndex);
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_send_mail:
                sendMail();
                return true;
            case R.id.action_cancel:
                mIsCancelling = true;
                finish();
            case R.id.action_next:
                moveNext();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void moveNext() {
        saveNote();

        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

        saveOriginalNoteValues();
        displayNote(mSpCourses, mEdtNoteTitle, mEdtNoteText);
        invalidateOptionsMenu();
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
        mNotePosition = intent.getIntExtra(NoteActivity.NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = mNotePosition ==POSITION_NOT_SET;
        if(mIsNewNote){
            createNewNote();
        }

        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
    }

    private void sendMail() {
        CourseInfo course = (CourseInfo) mSpCourses.getSelectedItem();
        String subject = mEdtNoteTitle.getText().toString();
        String text =   "Checkout what I learned in Pluralsight course \"" + course.getTitle()
                + "\"\n" + mEdtNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        if(mIsCancelling){
            if (mIsNewNote)
                DataManager.getInstance().removeNote(mNotePosition);
            else {
                restorePreviousNoteValues();
            }
        }
        else{
            saveNote();
        }
        super.onPause();
    }

    private void restorePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!=null)
            mViewModel.saveState(outState);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpCourses.getSelectedItem());
        mNote.setTitle(mEdtNoteTitle.getText().toString());
        mNote.setText(mEdtNoteText.getText().toString());
    }
}
