package com.example.notekeeper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import java.util.List;

import static com.example.notekeeper.NoteKeeperDatabaseContract.*;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String NOTE_ID = "com.example.notekeeper.NOTE_ID";
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
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
    public SimpleCursorAdapter mAdapterCourses;
    private boolean mCoursesQueryFinished;
    private boolean mNoteQueryFinished;

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


        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1},
                0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mSpCourses.setAdapter(mAdapterCourses);

        getSupportLoaderManager().initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();
        saveOriginalNoteValues();

        mEdtNoteTitle = findViewById(R.id.edt_title);
        mEdtNoteText = findViewById(R.id.edt_note);

        if(!mIsNewNote)
            getSupportLoaderManager().initLoader(LOADER_NOTES, null, this);

    }

    private void loadCourseData() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        mAdapterCourses.changeCursor(cursor);
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

        int courseIndex  =getIndexOfCourseId(courseId);
        mSpCourses.setSelection(courseIndex);
        mEdtNoteTitle.setText(noteTitle);
        mEdtNoteText.setText(noteText);
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more){
            String cursorCourseId = cursor.getString(courseIdPos);
            if(cursorCourseId.equals(courseId)){
                break;
            }
            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return  courseRowIndex;
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES){
            loader = createLoaderNotes();
        }
        else if(id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID
                };
                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                        null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
            }
        };
    }

    private CursorLoader createLoaderNotes() {
        mNoteQueryFinished = false;
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mOpenHelper.getReadableDatabase();

                String selection = NoteInfoEntry._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mNoteId)};

                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT,
                        NoteInfoEntry.COLUMN_COURSE_ID};
                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs,
                        null, null, null);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES){
            loadFinishedNotes(data);

        }

        else if(loader.getId() == LOADER_COURSES){
            mAdapterCourses.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }

    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCusor = data;
        mCourseIdPos = mNoteCusor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCusor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCusor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCusor.moveToNext();
        mNoteQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if(mNoteQueryFinished && mCoursesQueryFinished){
            displayNote();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES) {
            if (mNoteCusor != null)
                mNoteCusor.close();
        }else if(loader.getId() == LOADER_COURSES){
            mAdapterCourses.changeCursor(null);
        }
    }
}
