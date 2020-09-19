package com.delitx.flex.ui.activities

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.delitx.flex.R
import org.junit.Rule
import org.junit.Test

class RegistrationTest {
    @get:Rule
    val activityScenario = ActivityScenarioRule(Registration::class.java)

    @Test
    fun isDisplayedRight() {
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
        onView(withId(R.id.registration_text)).check(matches(isDisplayed()))
        onView(withId(R.id.registration_text)).check(matches(withText(R.string.sign_up)))
        onView(withId(R.id.email)).check(matches(isDisplayed()))
        onView(withId(R.id.email)).check(matches(withHint(R.string.email)))
        onView(withId(R.id.login)).check(matches(isDisplayed()))
        onView(withId(R.id.login)).check(matches(withHint(R.string.login)))
        onView(withId(R.id.password)).check(matches(isDisplayed()))
        onView(withId(R.id.password)).check(matches(withHint(R.string.password)))
        onView(withId(R.id.repeat_password)).check(matches(isDisplayed()))
        onView(withId(R.id.repeat_password)).check(matches(withHint(R.string.repeat_password)))
        onView(withId(R.id.sign_up_button)).check(matches(isDisplayed()))
        onView(withId(R.id.sign_up_button)).check(matches(isEnabled()))
        onView(withId(R.id.sign_up_button)).check(matches(withText(R.string.sign_up)))
        onView(withId(R.id.have_acc)).check(matches(isDisplayed()))
        onView(withId(R.id.have_acc)).check(matches(withText(R.string.already_account)))
        onView(withId(R.id.register_update_circle)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun switchToSignIn() {
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
        onView(withId(R.id.have_acc)).perform(click())
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
    }

    @Test
    fun switchToSignInAndPressBack() {
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
        onView(withId(R.id.have_acc)).perform(click())
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
        pressBack()
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
    }

    @Test
    fun switchToSignInAndSwitchBack() {
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
        onView(withId(R.id.have_acc)).perform(click())
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
        onView(withId(R.id.dont_acc)).perform(click())
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
    }
}