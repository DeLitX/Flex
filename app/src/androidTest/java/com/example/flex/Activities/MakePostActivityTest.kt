package com.example.flex.Activities

import android.app.Activity.RESULT_OK
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.example.flex.R
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test


class MakePostActivityTest {
    @get:Rule
    val activityScenario = ActivityScenarioRule(MakePostActivity::class.java)

    @get:Rule
    val intentsTestRule = IntentsTestRule(MakePostActivity::class.java)
    @get:Rule
    var mRuntimePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    fun isDisplayedRight() {
        onView(withId(R.id.make_post_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.publish_post_image)).check(matches(isDisplayed()))
        onView(withId(R.id.publish_post_text)).check(matches(isDisplayed()))
        onView(withId(R.id.button_submit_post)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.button_submit_post)).check(matches(withText(R.string.publish)))
        onView(withId(R.id.button_get_picture)).check(matches(isDisplayed()))
        onView(withId(R.id.button_get_picture)).check(matches(withText(R.string.get_picture)))
        onView(withId(R.id.button_take_picture)).check(matches(isDisplayed()))
        onView(withId(R.id.button_take_picture)).check(matches(withText(R.string.take_picture)))
    }

    @Test
    fun useCameraIntent() {
        val activityResult=createTakePhotoActivityResultStub()
        val expectedIntent: Matcher<Intent> = hasAction(MediaStore.ACTION_IMAGE_CAPTURE)
        intending(expectedIntent).respondWith(activityResult)

        onView(withId(R.id.button_take_picture)).perform(click())
        intended(expectedIntent)
    }
    /*@Test
    fun useGalleryIntent(){
        val expectedIntent=allOf(
            hasAction(Intent.ACTION_PICK),
            hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        )
        val activityResult=createGetPictureActivityResultStub()
        intending(expectedIntent).respondWith(activityResult)

        onView(withId(R.id.button_get_picture)).perform(click())
        intended(expectedIntent)
    }*/

    private fun createTakePhotoActivityResultStub(): Instrumentation.ActivityResult? {
        val bundle = Bundle()
        bundle.putParcelable(
            "data",
            BitmapFactory.decodeResource(
                intentsTestRule.activity.resources,
                R.drawable.ic_launcher_background
            )
        )
        val resultData=Intent()
        resultData.putExtras(bundle)
        return Instrumentation.ActivityResult(RESULT_OK,resultData)
    }
    private fun createGetPictureActivityResultStub():Instrumentation.ActivityResult{
        val resources:Resources=InstrumentationRegistry.getInstrumentation().context.resources
        val imageUri= Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+
                    resources.getResourcePackageName(R.drawable.ic_launcher_foreground)+"/"+
                    resources.getResourceTypeName(R.drawable.ic_launcher_foreground)+"/"+
                    resources.getResourceEntryName(R.drawable.ic_launcher_foreground)
        )
        val resultIntent=Intent()
        resultIntent.data=imageUri
        return Instrumentation.ActivityResult(RESULT_OK,resultIntent)
    }
}