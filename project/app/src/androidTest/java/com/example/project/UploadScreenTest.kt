package com.example.project

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.project.ui.screens.UploadScreen
import com.example.project.ui.theme.ProjectTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Test for UploadScreen - testing form submission and navigation
 *
 * This test simulates user interactions with the file upload form,
 * including button visibility, click actions, and navigation behavior.
 * It validates the complete user flow for uploading CSV files.
 */
@RunWith(AndroidJUnit4::class)
class UploadScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun uploadScreen_formSubmissionAndNavigation_worksCorrectly() {
        // Given: A flag to track navigation events
        var backButtonClicked = false

        // And: The upload screen is displayed
        composeTestRule.setContent {
            ProjectTheme {
                UploadScreen(
                    onBackClick = { backButtonClicked = true }
                )
            }
        }

        // When: The screen is loaded
        // Then: The title should be displayed
        composeTestRule
            .onNodeWithText("Upload")
            .assertExists()
            .assertIsDisplayed()

        // Then: The upload button should be visible and clickable
        composeTestRule
            .onNodeWithText("Upload from CSV")
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()

        // Then: Instructions should be displayed to guide the user
        composeTestRule
            .onNodeWithText("Upload a CSV file with glucose readings. The file will be saved locally and sent to the remote server for analysis.")
            .assertExists()
            .assertIsDisplayed()

        // Then: The back button should exist in the top bar
        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertExists()
            .assertIsDisplayed()

        // When: User clicks the back button for navigation
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        // Then: The navigation callback should be triggered
        assert(backButtonClicked) { "Back button should trigger navigation callback" }

        // When: The upload button is clicked (would open file picker in real scenario)
        // Note: We can't test the actual file picker in UI tests as it's a system dialog,
        // but we can verify the button is clickable and performs an action
        composeTestRule
            .onNodeWithText("Upload from CSV")
            .performClick()

        // Then: The UI should remain stable (no crashes)
        composeTestRule
            .onNodeWithText("Upload from CSV")
            .assertExists()
    }
}
