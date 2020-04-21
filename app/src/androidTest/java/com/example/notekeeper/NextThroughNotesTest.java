package com.example.notekeeper;

import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import junit.extensions.ActiveTestSuite;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static org.hamcrest.Matcher.*;
import static androidx.test.espresso.Espresso.pressBack;

public class NextThroughNotesTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void NextThroughNotes(){
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));

        onView(withId(R.id.list_items)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));


        List<NoteInfo> noteInfos = DataManager.getInstance().getNotes();
        for(int index = 0; index < noteInfos.size(); index++){
            NoteInfo note = noteInfos.get(index);

            onView(withId(R.id.sp_courses)).check(
                    ViewAssertions.matches(withSpinnerText(note.getCourse().getTitle()))
            );
            onView(withId(R.id.tv_course)).check(ViewAssertions.matches(withText(note.getTitle())));
            onView(withId(R.id.tv_note)).check(ViewAssertions.matches(withText(note.getText())));

            onView(withId(R.id.action_next)).perform(click());
        }

    }
}