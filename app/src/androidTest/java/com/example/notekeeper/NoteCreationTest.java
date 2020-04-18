package com.example.notekeeper;


import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import junit.extensions.ActiveTestSuite;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static org.hamcrest.Matcher.*;
import static androidx.test.espresso.Espresso.pressBack;


@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp(){
        sDataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityTestRule<NoteListAcitivity> mNoteListAcitivityRule =
            new ActivityTestRule<>(NoteListAcitivity.class);

    @Test
    public void createNewNote(){
        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of test";

//        ViewInteraction fabNewNote = onView(withId(R.id.fab));
//        fabNewNote.perform(click());
        onView(withId(R.id.fab)).perform(click());


        onView(withId(R.id.edt_title)).perform(typeText(noteTitle));
        onView(withId(R.id.edt_note)).perform(typeText(noteText), closeSoftKeyboard());

        pressBack();
    }
}