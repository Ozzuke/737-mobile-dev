package com.example.project

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.project.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test login flow: toomas/toomas123 -> profile screen shows username
 */
@RunWith(AndroidJUnit4::class)
class LoginFlowUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginFlowTest_loginAndCheckProfile() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("usernameField")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("usernameField")
            .performTextInput("toomas")

        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("toomas123")

        composeTestRule.onNodeWithTag("loginButton")
            .assertIsEnabled()
            .performClick()

        // wait for home screen
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("profileButton")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("profileButton")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("profileUsername")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // check that username shows up
        composeTestRule.onNodeWithTag("profileUsername")
            .assertExists()
            .assertTextContains("toomas", substring = true, ignoreCase = true)
    }
}

