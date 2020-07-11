package com.example.notekeeper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.example.notekeeper.NoteKeeperProviderContract.Courses;

import static com.example.notekeeper.NoteKeeperProviderContract.*;

public class NoteKeeperProvider extends ContentProvider {

    public static final String MIME_VENDOR_TYPE = "vnd"
            + AUTHORITY + ".";
    private NoteKeeperOpenHelper mNoteKeeperOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 2;

    public static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        SQLiteDatabase db = mNoteKeeperOpenHelper.getWritableDatabase();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + " = ?";
                String[] rowSelectionArgs = {Long.toString(rowId)};
                return db.delete(NoteInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
            case NOTES:
                break;
            case COURSES:
                break;
            case NOTES_EXPANDED:
                break;
        }
        return -1;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE +Notes.PATH;
                break;
            case COURSES:
                //vnd.android.cusor.dir/vnd.com.example.notekeeper.provider.courses
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Courses.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE +Notes.PATH_EXPANDED;
                break;
            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"  + MIME_VENDOR_TYPE + Notes.PATH;
        }
        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        long rowId = -1;
        SQLiteDatabase db = mNoteKeeperOpenHelper.getWritableDatabase();
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;
            // just in case some stupid developers try to insert to the expanded table
            case NOTES_EXPANDED:
                break;
        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mNoteKeeperOpenHelper =  new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mNoteKeeperOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + " = ?";
                String[] rowSelectionArgs = {Long.toString(rowId)};
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection,rowSelection, rowSelectionArgs,
                        null, null, null);
        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String[] columns = new String[projection.length];
        for(int i = 0; i < projection.length; i++){
            columns[i] = projection[i].equals(BaseColumns._ID) ||
                    projection[i].equals(CourseIdColumns.COLUMN_COURSE_ID)?
                    NoteInfoEntry.getQName(projection[i]) : projection[i];
        }

        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN "+
                CourseInfoEntry.TABLE_NAME + " ON "  +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) +  " = " +
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);
        return db.query(tablesWithJoin, columns, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        SQLiteDatabase db = mNoteKeeperOpenHelper.getWritableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case NOTES:
                return db.update(NoteInfoEntry.TABLE_NAME, values,selection, selectionArgs);
        }
        return -1;
    }
}
