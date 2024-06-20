package com.softwaredesign.novelreader;
// Import necessary classes for Android testing

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.softwaredesign.novelreader.Activities.DetailActivity;
import com.softwaredesign.novelreader.Fragments.ChapterListFragment;
import com.softwaredesign.novelreader.Models.NovelDescriptionModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RunWith(AndroidJUnit4.class)
public class DetailActivityIntegrationTest {

    // Initialize Intents before each test
    @Before
    public void setUp() {
        Intents.init();
    }

    // Release Intents after each test
    @After
    public void tearDown() {
        Intents.release();
    }

    // Test the DetailActivity's onCreate method
    // The DetailActivity should display the detail of the novel
    // The detail should include the novel's image, name, and author
    @Test
    public void testOnCreate() {
        Intent intent = new Intent();
        intent.setClassName("com.softwaredesign.novelreader", "com.softwaredesign.novelreader.Activities.DetailActivity");
        Bundle bundle = new Bundle();


        bundle.putString("NovelUrl", "https://truyenfull.vn");
        intent.putExtras(bundle);

        // Launch DetailActivity and check if the views are displayed
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            Espresso.onView(withId(R.id.detailImage)).check(matches(isDisplayed()));
            Espresso.onView(withId(R.id.detailName)).check(matches(isDisplayed()));
            Espresso.onView(withId(R.id.detailAuthor)).check(matches(isDisplayed()));
        }
    }

    // Test if the onBackPressed method finishes the activity
    @Test
    public void testOnBackPressed() {
        // Create an Intent to launch DetailActivity
        Intent intent = new Intent();
        intent.setClassName("com.softwaredesign.novelreader", "com.softwaredesign.novelreader.Activities.DetailActivity");
        Bundle bundle = new Bundle();


        bundle.putString("NovelUrl", "https://truyenfull.vn");
        intent.putExtras(bundle);


        // Launch DetailActivity and check if the activity finishes on back press
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                activity.onBackPressed();
                assertTrue(activity.isFinishing());
            });
        }
    }

    // Test if the handleBottomNav method correctly switches fragments
    // based on the selected item in the bottom navigation view.
    @Test
    public void testHandleBottomNav() {

        // Create an Intent to launch DetailActivity
        Intent intent = new Intent();
        intent.setClassName("com.softwaredesign.novelreader", "com.softwaredesign.novelreader.Activities.DetailActivity");
        Bundle bundle = new Bundle();

        bundle.putString("NovelUrl", "https://truyenfull.vn");
        intent.putExtras(bundle);

        // Launch DetailActivity and check if the views are displayed
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {

            scenario.onActivity(activity -> {

                BottomNavigationView bottomNavigationView = activity.findViewById(R.id.detailBottomNav);
                bottomNavigationView.setSelectedItemId(R.id.detailBottomNavChapterList);
                activity.getSupportFragmentManager().executePendingTransactions();
                Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.detailFrameLayout);
                assertTrue(currentFragment instanceof ChapterListFragment);

            });
        }
    }

    // Test if the setUIData method correctly sets the UI data.
    @Test
    public void testSetUIData() {

        // Create an Intent to launch DetailActivity

        Intent intent = new Intent();
        intent.setClassName("com.softwaredesign.novelreader", "com.softwaredesign.novelreader.Activities.DetailActivity");
        Bundle bundle = new Bundle();
        bundle.putString("NovelUrl", "https://truyenfull.vn");
        intent.putExtras(bundle);


        // Launch DetailActivity and check if the UI data is set correctly
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {

            scenario.onActivity(activity -> {
                try {

                    // Use reflection to access private method
                    Method method = DetailActivity.class.getDeclaredMethod("setUIData", NovelDescriptionModel.class);

                    // Set the method to be accessible
                    method.setAccessible(true);

                    // Invoke the method with a NovelDescriptionModel object
                    NovelDescriptionModel ndm = new NovelDescriptionModel("Test Name", "Test Author", "Test Description", "Test Image URL");
                    method.invoke(activity, ndm);

                    // Check if the UI data is set correctly
                    TextView detailName = activity.findViewById(R.id.detailName);
                    TextView detailAuthor = activity.findViewById(R.id.detailAuthor);
                    assertEquals("Test Name", detailName.getText().toString());
                    assertEquals("Test Author", detailAuthor.getText().toString());


                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    fail(e.getMessage());
                }
            });
        }
    }


    // Test if the getNovelUrlFromPreviousIntent method correctly retrieves the URL
    @Test
    public void testGetNovelUrlFromPreviousIntent() {

        // Create an Intent to launch DetailActivity

        Intent intent = new Intent();
        intent.setClassName("com.softwaredesign.novelreader", "com.softwaredesign.novelreader.Activities.DetailActivity");
        Bundle bundle = new Bundle();
        bundle.putString("NovelUrl", "https://truyenfull.vn/chuong-1/");
        intent.putExtras(bundle);


        // Launch DetailActivity and check if the URL is retrieved correctly
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                try {
                    Method method = DetailActivity.class.getDeclaredMethod("getNovelUrlFromPreviousIntent", Bundle.class);
                    Field field = DetailActivity.class.getDeclaredField("NovelUrl");
                    method.setAccessible(true);
                    field.setAccessible(true);
                    method.invoke(activity, bundle);
                    assertEquals("https://truyenfull.vn/", field.get(activity));
                } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException |
                         InvocationTargetException e) {
                    fail(e.getMessage());
                }
            });
        }
    }

    // Test if the view components are initialized correctly
    @Test
    public void testViewInit() {

        // Create an Intent to launch DetailActivity
        Intent intent = new Intent();
        intent.setClassName("com.softwaredesign.novelreader", "com.softwaredesign.novelreader.Activities.DetailActivity");
        Bundle bundle = new Bundle();
        bundle.putString("NovelUrl", "https://truyenfull.vn");
        intent.putExtras(bundle);


        // Launch DetailActivity and check if the view components are initialized
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                try {
                    // Use reflection to access private fields
                    Field detailImageField = DetailActivity.class.getDeclaredField("detailImage");
                    Field detailNameField = DetailActivity.class.getDeclaredField("detailName");
                    Field detailAuthorField = DetailActivity.class.getDeclaredField("detailAuthor");
                    Field bottomNavigationViewField = DetailActivity.class.getDeclaredField("bottomNavigationView");

                    // Set the fields to be accessible
                    detailImageField.setAccessible(true);
                    detailNameField.setAccessible(true);
                    detailAuthorField.setAccessible(true);
                    bottomNavigationViewField.setAccessible(true);

                    // Get the field values
                    ImageView detailImage = (ImageView) detailImageField.get(activity);
                    TextView detailName = (TextView) detailNameField.get(activity);
                    TextView detailAuthor = (TextView) detailAuthorField.get(activity);
                    BottomNavigationView bottomNavigationView = (BottomNavigationView) bottomNavigationViewField.get(activity);

                    // Check if the fields are not null
                    assertNotNull(detailImage);
                    assertNotNull(detailName);
                    assertNotNull(detailAuthor);
                    assertNotNull(bottomNavigationView);

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    fail(e.getMessage());
                }
            });
        }
    }


    // Test if the toggleDetailVisibility method correctly toggles the visibility of the detail view

    @Test
    public void testToggleDetailVisibility() {

        // Create an Intent to launch DetailActivity

        Intent intent = new Intent();
        intent.setClassName("com.softwaredesign.novelreader", "com.softwaredesign.novelreader.Activities.DetailActivity");
        Bundle bundle = new Bundle();

        bundle.putString("NovelUrl", "https://truyenfull.vn");
        intent.putExtras(bundle);


        // Launch DetailActivity and check if the detail view visibility is toggled
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                try {

                    // Use reflection to access private method
                    Method method = DetailActivity.class.getDeclaredMethod("toggleDetailVisibility", boolean.class);
                    method.setAccessible(true);

                    // Invoke the method with a boolean value
                    method.invoke(activity, false);

                    // Check if the detail view is hidden
                    ImageView detailImage = activity.findViewById(R.id.detailImage);
                    TextView detailName = activity.findViewById(R.id.detailName);
                    TextView detailAuthor = activity.findViewById(R.id.detailAuthor);

                    assertFalse(detailImage.isShown());
                    assertFalse(detailName.isShown());
                    assertFalse(detailAuthor.isShown());


                } catch (NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException e) {
                    fail(e.getMessage());
                }
            });
        }
    }


    // Test if the loadFragment method correctly loads the fragment

    @Test
    public void testLoadFragment() {

        // Create an Intent to launch DetailActivity
        Intent intent = new Intent();
        intent.setClassName("com.softwaredesign.novelreader", "com.softwaredesign.novelreader.Activities.DetailActivity");
        Bundle bundle = new Bundle();


        bundle.putString("NovelUrl", "https://truyenfull.vn");
        intent.putExtras(bundle);


        // Launch DetailActivity and check if the fragment is loaded correctly
        try (ActivityScenario<DetailActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                try {

                    Method method = DetailActivity.class.getDeclaredMethod("loadFragment", Fragment.class);
                    method.setAccessible(true);
                    Fragment fragment = new ChapterListFragment();
                    method.invoke(activity, fragment);
                    activity.getSupportFragmentManager().executePendingTransactions();
                    Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.detailFrameLayout);
                    assertTrue(currentFragment instanceof ChapterListFragment);

                    
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    fail(e.getMessage());
                }
            });
        }
    }
}