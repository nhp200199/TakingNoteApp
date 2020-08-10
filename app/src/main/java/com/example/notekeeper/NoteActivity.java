package com.example.notekeeper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.notekeeper.NoteKeeperProviderContract.Courses;
import com.example.notekeeper.NoteKeeperProviderContract.Notes;

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
    private Uri mNoteUri;
    private NotificationManagerCompat mManagerCompat;

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
            case R.id.action_set_reminder:
                showReminderNotification();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showReminderNotification() {
        String noteText = mEdtNoteText.getText().toString();
        String noteTitle = mEdtNoteTitle.getText().toString();
        int noteId = (int)ContentUris.parseId(mNoteUri);

        Intent intent = new Intent(this, NoteReminderReceiver.class);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        long currentTimeInMilliseconds = SystemClock.elapsedRealtime();
        long TEN_SECONDS = 10 * 1000;

        long alarmTime = currentTimeInMilliseconds + TEN_SECONDS;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent);
    }

    private Notification createNotification(String noteTitle, String noteText) {
        Intent noteActivityIntent = new Intent(getApplicationContext(), NoteActivity.class);
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, mNoteId);
        final Resources res = getApplicationContext().getResources();

        Intent noteBackupService = new Intent(getApplicationContext(), NoteBackupService.class);
        noteBackupService.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.logo);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NoteReminderNotification.NOTIFICATION_TAG)
                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_stat_note_reminder)
                .setContentTitle(noteTitle)
                .setContentText(noteText)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(Notification.PRIORITY_DEFAULT)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(picture)

                // Set ticker text (preview) information for this notification.
                .setTicker(noteTitle)

                .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(noteText)
                            .setBigContentTitle(noteTitle)
                            .setSummaryText("Review note"))
                .setContentIntent(PendingIntent.getActivity(
                        this,
                        0,
                        noteActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                ))
                .addAction(new NotificationCompat.Action(
                        0,
                        "View all notes",
                        PendingIntent.getActivity(
                                getApplicationContext(),
                                0,
                                new Intent(getApplicationContext(), MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT)))
                .addAction(new NotificationCompat.Action(
                        0,
                        "Backup Notes",
                        PendingIntent.getService(
                                getApplicationContext(),
                                0,
                                noteBackupService,
                                PendingIntent.FLAG_UPDATE_CURRENT)
                        )
                )
                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);;

        return builder.build();
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

        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, noteTitle);
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
        //Here we can and should interact with the Content Provider in a diffent thread
        AsyncTask<ContentValues, Integer, Uri> task = new AsyncTask<ContentValues, Integer, Uri>() {
            private ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                Log.d("Note Activity", "Call to execute - thread: " + Thread.currentThread().getId());
                ContentValues insertValues = contentValues[0];
                Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);
                publishProgress(2);
                return rowUri;
//                Q: Why dont we assign rowUri to mNoteUri?
//                A: We can, it is legal, but not a good practice. Remember thread behavior?
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                mProgressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                Log.d("Note Activity", "Call to execute - thread: " + Thread.currentThread().getId());
                mNoteUri = uri;
                mProgressBar.setVisibility(View.GONE);
            }
        };

        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        Log.d("Note Activity", "Call to execute - thread: " + Thread.currentThread().getId());
        task.execute(values);
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
                deleteNoteFromDatabase();
            else {
                restorePreviousNoteValues();
            }
        }
        else{
            saveNote();
        }
        super.onPause();
    }

    private void deleteNoteFromDatabase() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().delete(mNoteUri, null, null);
                return null;
            }
        };
        task.execute();

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
        String courseId = selectedCourseId();
        String noteTitle = mEdtNoteTitle.getText().toString();
        String noteText = mEdtNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText){
        final String selection = Notes._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        final ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().update(Notes.CONTENT_URI, values,selection, selectionArgs);
                return null;
            }
        };
        task.execute();
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
        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };
        return  new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);
    }

    private CursorLoader createLoaderNotes() {
        mNoteQueryFinished = false;
        String[] noteColumns = {
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT,
                Notes.COLUMN_COURSE_ID};
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri, noteColumns, null, null, null);
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

    @Override
    public void onBackPressed() {
        mIsCancelling = false;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        //loader có thể lưu lại dữ liệu cho ta
        //khi ctrinh chaỵ vào onStart  lại thì loader đã gọi lại hàm onFinished(), trong đó có hàm moveToNext() đứng đó sẽ làm loader tiến thêm 1 bước => lỗi
        mNoteQueryFinished = false;
        mCoursesQueryFinished = false;
        mNoteCusor.moveToPrevious();
        super.onStop();
    }
}