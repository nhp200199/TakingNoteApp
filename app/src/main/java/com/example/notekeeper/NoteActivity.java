package com.example.notekeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import static com.example.notekeeper.NoteKeeperDatabaseContract.*;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_ID = "com.example.notekeeper.NOTE_ID";
    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private Spinner mSpCourses;
    private EditText mEdtNoteTitle;
    private EditText mEdtNoteText;
    private boolean mIsNewNote;
    private int mNoteId;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;
    public NoteKeeperOpenHelper mOpenHelper;
    public Cursor mNoteCusor;
    public int mCourseIdPos;
    public int mNoteTitlePos;
    public int mNoteTextPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOpenHelper = new NoteKeeperOpenHelper(this);

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
            loadNoteData();
        }

    }

    private void loadNoteData() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
                NoteInfoEntry.COLUMN_COURSE_ID};
        mNoteCusor =    db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs,
                null, null, null);
        mCourseIdPos = mNoteCusor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCusor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCusor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCusor.moveToNext();
        displayNote();
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
        item.setEnabled(mNoteId < lastIndex);
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
                return true;
            case R.id.action_next:
                moveNext();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void moveNext() {
        saveNote();

        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    private void displayNote() {
        String courseId = mNoteCusor.getString(mCourseIdPos);
        String noteTitle = mNoteCusor.getString(mNoteTitlePos);
        String noteText = mNoteCusor.getString(mNoteTextPos);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        int courseIndex  =courses.indexOf(course);
        mSpCourses.setSelection(courseIndex);
        mEdtNoteTitle.setText(noteTitle);
        mEdtNoteText.setText(noteText);
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NoteActivity.NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if(mIsNewNote){
            createNewNote();
        }

//        mNote = DataManager.getInstance().getNotes().get(mNoteId);

    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNoteId = dm.createNewNote();
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
                DataManager.getInstance().removeNote(mNoteId);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOpenHelper.close();
    }
}
