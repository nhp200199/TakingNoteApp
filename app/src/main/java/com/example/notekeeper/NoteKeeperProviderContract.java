package com.example.notekeeper;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NoteKeeperProviderContract {
    private NoteKeeperProviderContract(){}
    public static final String AUTHORITY = "com.example.notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);


    protected interface CourseIdColumns{
        public static final String COLUMN_COURSE_ID = "course_id";
    }
    protected interface CoursesColumns{
        public static final String COLUMN_COURSE_TITLE= "course_title";
    }
    protected interface NotesColumns{
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_title";
    }

    public static final class Courses implements BaseColumns, CourseIdColumns, CoursesColumns {
        public static final String PATH = "courses";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

//    in the Notes class, we implement the Course Columns so that we can have access to the title of each course
    public static final class Notes implements BaseColumns, CourseIdColumns, NotesColumns, CoursesColumns{
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
        public static final String PATH_EXPANDED = "notes_expanded";
        public static final Uri CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED);
    }

}
