package com.project.notepad;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.base.DefaultFailureHandler;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class Tester {
    @Rule
    public ActivityTestRule<NoteListActivity> mActivityTestRule = new ActivityTestRule<>(NoteListActivity.class);
    public NoteListActivity mNoteListActivity;
    private String mNoteTitle , mNoteOldText , mNoteNewText , mFinalNoteText;
    @Before
    public void setUp(){
        mNoteListActivity = mActivityTestRule.getActivity();
        mNoteTitle = "Asyn";
        mNoteOldText = "Hi.";
        mNoteNewText = "Bye.";
        mFinalNoteText = "Hi.\nBye.";
    }

    @Test
    public void deleteNoteWhenNoTitleAndText() {
        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(ViewActions.click());
        closeSoftKeyboard();
        pressBack();
        try {
            onView(withId(R.id.list_notes)).check(ViewAssertions.matches(ViewMatchers.withText("")));
        }catch(Error ex){
            return;
        }
        throw new IllegalStateException("Note with no title exists");
    }

    @Test
    public void deleteNoteWhenDuplicate(){
        deleteNote();
        createNote();
        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.note_title)).perform(ViewActions.typeText(mNoteTitle));
        Espresso.onView(ViewMatchers.withId(R.id.note_text)).perform(ViewActions.typeText(mNoteNewText));
        Espresso.closeSoftKeyboard();
        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withText(mNoteTitle)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.note_text)).check(matches(ViewMatchers.withText(mFinalNoteText)));
    }

    private void deleteNote() {
        try {
            Espresso.onView(ViewMatchers.withText(mNoteTitle)).perform(ViewActions.click());
        }catch (NoMatchingViewException exception) {
            return;
        }
        ViewInteraction overflowMenuButton = onView(
                allOf(withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        2),
                                2),
                        isDisplayed()));
        overflowMenuButton.perform(click());
        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Delete"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());
    }

    private void createNote() {
        Espresso.onView(ViewMatchers.withId(R.id.fab)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.note_title)).perform(ViewActions.typeText(mNoteTitle));
        Espresso.onView(ViewMatchers.withId(R.id.note_text)).perform(ViewActions.typeText(mNoteOldText));
        Espresso.closeSoftKeyboard();
        Espresso.pressBack();
    }

    public static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
