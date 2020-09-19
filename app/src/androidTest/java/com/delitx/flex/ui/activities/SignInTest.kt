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

class SignInTest {
    @get:Rule
    val activityScenario = ActivityScenarioRule(SignIn::class.java)

    @Test
    fun isDisplayedRight() {
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
        onView(withId(R.id.registration_text)).check(matches(isDisplayed()))
        onView(withId(R.id.registration_text)).check(matches(withText(R.string.sign_in)))
        onView(withId(R.id.login)).check(matches(isDisplayed()))
        onView(withId(R.id.login)).check(matches(withHint(R.string.login)))
        onView(withId(R.id.password)).check(matches(isDisplayed()))
        onView(withId(R.id.password)).check(matches(withHint(R.string.password)))
        onView(withId(R.id.sign_in_button)).check(matches(isDisplayed()))
        onView(withId(R.id.sign_in_button)).check(matches(withText(R.string.sign_in)))
        onView(withId(R.id.dont_acc)).check(matches(isDisplayed()))
        onView(withId(R.id.dont_acc)).check(matches(withText(R.string.dont_have_account)))
        onView(withId(R.id.login_update_circle)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun switchToRegistration() {
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
        onView(withId(R.id.dont_acc)).perform(click())
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
    }

    @Test
    fun switchToRegistrationPressBack() {
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
        onView(withId(R.id.dont_acc)).perform(click())
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
        pressBack()
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
    }

    @Test
    fun switchToRegistrationSwitchBack() {
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
        onView(withId(R.id.dont_acc)).perform(click())
        onView(withId(R.id.registration)).check(matches(isDisplayed()))
        onView(withId(R.id.have_acc)).perform(click())
        onView(withId(R.id.sign_in)).check(matches(isDisplayed()))
    }
}