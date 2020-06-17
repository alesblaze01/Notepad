package com.project.notepad;


import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.project.notepad.Contract.NoteContentContract;
import com.project.notepad.Contract.NoteContentContract.Courses;

import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class NoteListActivityTest2 {

    @Rule
    public ActivityTestRule<NoteListActivity> mActivityTestRule = new ActivityTestRule<>(NoteListActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.INTERNET",
                    "android.permission.ACCESS_NETWORK_STATE",
                    "android.permission.WRITE_EXTERNAL_STORAGE");
    public NoteListActivity mNoteListActivity;
    @Before
    public void setUp(){
        mNoteListActivity = mActivityTestRule.getActivity();
    }
    @Test
    public void checkSaveNoteTitleChange() {
        onView(withId(R.id.list_notes)).perform(actionOnItemAtPosition(1,click()));

        onView(withId(R.id.note_title)).perform(replaceText("Service default threads "));
        closeSoftKeyboard();
        pressBack();

        onView(withId(R.id.list_notes)).perform(actionOnItemAtPosition(1,click()));

        final ViewInteraction noteTitle = onView(withId(R.id.note_title));
        noteTitle.check(ViewAssertions.matches(withText("Service default threads ")));
    }

    @Test
    public void checkSaveNoteTextChange() {
        final ViewInteraction recyclerView = onView(withId(R.id.list_notes));
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        onView(withId(R.id.note_text)).perform(replaceText("lhiuhiahsidfuhsadjfisdf oasjdfsiadjf;oisdja aiojoasjdf"));
        closeSoftKeyboard();
        pressBack();
//        onView(withText("Note Saved")).check(ViewAssertions.matches(Matchers.instanceOf(Toast.class)));

        recyclerView.perform(actionOnItemAtPosition(1, click()));
        onView(withId(R.id.note_text)).check(ViewAssertions.matches(withText("lhiuhiahsidfuhsadjfisdf oasjdfsiadjf;oisdja aiojoasjdf")));
        closeSoftKeyboard();
        pressBack();
    }

//    @Test
//    public void checkSavedCourseSpinnerSelection(){
//        final ViewInteraction recycler = onView(withId(R.id.list_notes));
//        recycler.perform(actionOnItemAtPosition(1,click()));
//
//        final ViewInteraction spinner = onView(withId(R.id.course_spinner));
//        spinner.perform(ViewActions.click());
//
//        String[] projection = {
//                Courses.COURSE_TITLE
//        };
//        String orderBy = Courses.COURSE_TITLE;
//        final Cursor query = mNoteListActivity.getContentResolver().
//                query(Courses.CONTENT_URI, projection, null, null, orderBy);
//        String[] courses = new String[query.getCount()];
//        int index = 0;
//        while (query.moveToNext()) {
//            courses[index++] = query.getString(query.getColumnIndex(Courses.COURSE_TITLE));
//        }
////        onData(is(courses[2])).perform(click());
//        onData(Matchers.containsString("Android Programming with Intents")).perform(click());
//
//    }

//    private static Matcher<View> childAtPosition(
//            final Matcher<View> parentMatcher, final int position) {
//
//        return new TypeSafeMatcher<View>() {
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("Child at position " + position + " in parent ");
//                parentMatcher.describeTo(description);
//            }
//
//            @Override
//            public boolean matchesSafely(View view) {
//                ViewParent parent = view.getParent();
//                return parent instanceof ViewGroup && parentMatcher.matches(parent)
//                        && view.equals(((ViewGroup) parent).getChildAt(position));
//            }
//        };
//    }
}
