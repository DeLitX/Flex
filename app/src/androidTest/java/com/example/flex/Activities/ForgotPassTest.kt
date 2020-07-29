package com.example.flex.Activities

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.flex.R
import org.junit.Rule
import org.junit.Test


class ForgotPassTest{
    @get:Rule
    val activityScenario=ActivityScenarioRule(ForgotPass::class.java)
    @Test
    fun isDisplayedRight(){
        onView(withId(R.id.forgot_pass_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.forgot_pass_text)).check(matches(isDisplayed()))
        onView(withId(R.id.forgot_pass_text)).check(matches(withText(R.string.enter_email)))
        onView(withId(R.id.forgot_pass_email)).check(matches(isDisplayed()))
        onView(withId(R.id.forgot_pass_email)).check(matches(withHint(R.string.email)))
        onView(withId(R.id.forgot_pass_button)).check(matches(isDisplayed()))
        onView(withId(R.id.forgot_pass_button)).check(matches(withText(R.string.send_code_email)))
        onView(withId(R.id.change_pass_code)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.change_pass_button)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.change_pass_pass)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}