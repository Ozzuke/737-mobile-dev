## Step 4 Report — UI polish, themes, tests & release

### Testing strategy
A unit test verifies glucose conversion logic (for example, mg/dL ↔ mmol/L) to ensure data correctness and rounding behavior. A UI test simulates a user logging in and verifies that the username shown on the profile screen matches the signed-in account.

### Build process for APK
The signed release APK was produced using Android Studio's built-in Generate Signed Bundle / APK workflow. The keystore was created through the IDE wizard and stored privately; the signed APK output was copied to the project's artifacts for inclusion with the release deliverables.

### Known bugs or limitations
The LLM-generated summary currently uses the word "recommendations," which can imply medical advice; it should be replaced with neutral terms (plural) such as "suggestions", "guidance", or "informational notes" to avoid clinical implications since the app is educational only. The app can behave inconsistently when the system is already in dark mode, theme detection sometimes yields mixed UI states. Also, tapping the info button may report "0 readings" and "0 intervals" even after a file has been uploaded, indicating the UI isn't refreshing the displayed import summary correctly.

