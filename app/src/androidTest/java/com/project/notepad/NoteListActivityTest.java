package com.project.notepad;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.project.notepad.Utility.DataManager;
import com.project.notepad.Model.NoteInfo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class NoteListActivityTest {
    @Rule
    public ActivityTestRule mActivityTestRule = new ActivityTestRule(NoteListActivity.class);

    @Test
    public void NextThroughNotes(){
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_bar)).perform(NavigationViewActions.navigateTo(R.id.nav_notes));
        onView(withId(R.id.list_notes)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        for (int i = 0 ; i < notes.size() ; i++) {
            NoteInfo noteInfo = notes.get(i);
            onView(withId(R.id.course_spinner)).check(matches(withSpinnerText(noteInfo.getCourse().getTitle())));
            onView(withId(R.id.note_title)).check(matches(withText(noteInfo.getTitle())));
            onView(withId(R.id.note_text)).check(matches(withText(noteInfo.getText())));
            if (i < notes.size()-1)
                onView(allOf(withId(R.id.action_next_note),isEnabled())).perform(click());
            else
                onView(withId(R.id.action_next_note)).check(doesNotExist());
        }

    }

}