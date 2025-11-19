package com.example.project

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.project.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI test for testing the login flow.
 * Tests logging in with username "toomas" and password "toomas123",
 * navigating to the profile screen, and verifying the username is present.
 */
@RunWith(AndroidJUnit4::class)
class LoginFlowUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginFlowTest_loginAndCheckProfile() {
        // Wait for the login screen to appear
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("usernameField")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Enter username "toomas"
        composeTestRule.onNodeWithTag("usernameField")
            .performTextInput("toomas")

        // Enter password "toomas123"
        composeTestRule.onNodeWithTag("passwordField")
            .performTextInput("toomas123")

        // Click the login button
        composeTestRule.onNodeWithTag("loginButton")
            .assertIsEnabled()
            .performClick()

        // Wait for navigation to home screen after successful login
        // The home screen should have the profile button visible
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("profileButton")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click on the profile button to navigate to the profile screen
        composeTestRule.onNodeWithTag("profileButton")
            .performClick()

        // Wait for the profile screen to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithTag("profileUsername")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify that the username "toomas" is present in the profile screen
        // The username is displayed with @ prefix, so we check for "@toomas"
        composeTestRule.onNodeWithTag("profileUsername")
            .assertExists()
            .assertTextContains("toomas", substring = true, ignoreCase = true)
    }
}

