# Next Steps: Enhanced Home Screen Integration

This document outlines the roadmap for fully integrating the home screen with the CGM API to create the complete glucose monitoring experience.

---

## Current Status

✅ **Completed**:
- API integration with Retrofit
- Datasets screen showing all uploaded data
- Analysis screen with AI-powered insights
- Error handling and loading states
- Disclaimer dialog
- CSV upload endpoint prepared

🔲 **Pending**:
- Home screen displays latest glucose data from API
- Real-time status indicator
- Interactive glucose graph
- Offline support with cached data
- Automatic refresh on app launch
- Settings screen for dataset selection

---

## Phase 1: Home Screen API Integration

### 1.1 Create MainViewModel

**File**: `ui/viewmodels/MainViewModel.kt`

**Responsibilities**:
- Fetch latest dataset on app launch
- Get dataset data (glucose overlay) for graph
- Get analysis for status indicator
- Manage online/offline state
- Auto-refresh logic
- Save data to Room for offline access

**State Management**:
```kotlin
data class HomeScreenState(
    val isOnline: Boolean,
    val latestGlucose: Double?,
    val timestamp: String?,
    val status: RatingCategory?,
    val overlayData: List<OverlayDay>,
    val selectedPreset: String,
    val isLoading: Boolean,
    val error: String?
)
```

### 1.2 Update CgmApiRepository

Add methods:
```kotlin
suspend fun getLatestDataset(): Result<DatasetSummary>
suspend fun getLatestGlucoseValue(datasetId: String): Result<Double>
```

### 1.3 Restructure HomeScreen.kt

**Layout**:
```
┌─────────────────────────────────┐
│  TopBar [Offline Indicator]     │
├─────────────────────────────────┤
│  Current Glucose Card            │
│  ┌─────────────────┬─────────┐ │
│  │ 6.5 mmol/L      │ 🟢 GOOD │ │  ← Click opens analysis
│  │ Just now        │         │ │
│  └─────────────────┴─────────┘ │
├─────────────────────────────────┤
│  Timeframe Selector              │
│  [ 24h ] [ 7d ] [ 14d ]         │
├─────────────────────────────────┤
│  Glucose Graph                   │
│  ┌─────────────────────────────┐│
│  │ Interactive graph            ││  ← Click opens metrics
│  │ Shows glucose over time      ││
│  └─────────────────────────────┘│
├─────────────────────────────────┤
│  Upload CSV Button               │
└─────────────────────────────────┘
```

**Implementation Steps**:
1. Replace example widgets with API-driven components
2. Add LaunchedEffect to fetch data on screen load
3. Observe MainViewModel state
4. Display glucose value + status indicator
5. Add click handlers for status and graph

### 1.4 Create GlucoseGraphComponent

**File**: `ui/components/GlucoseGraphComponent.kt`

**Features**:
- Line chart showing glucose over time
- X-axis: Time (hours/days based on preset)
- Y-axis: Glucose (mmol/L)
- Color zones: Normal (green), Warning (yellow), Critical (red)
- Clickable to open detailed metrics

**Libraries to Consider**:
- **Vico** (modern Compose charting library)
- **MPAndroidChart** (mature but not Compose-native)
- **Custom Canvas** drawing (educational but time-consuming)

**Simple Implementation** (using Canvas):
```kotlin
@Composable
fun GlucoseGraph(
    data: List<GlucosePoint>,
    preset: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(200.dp)
        .clickable(onClick = onClick)
    ) {
        // Draw background zones (green/yellow/red)
        // Plot glucose points as line chart
        // Add axis labels
    }
}
```

---

## Phase 2: Offline Support

### 2.1 Add Room Entities for API Data

**File**: `data/local/entity/ApiCacheEntity.kt`

```kotlin
@Entity(tableName = "api_cache")
data class ApiCacheEntity(
    @PrimaryKey val key: String,
    val data: String, // JSON string
    val timestamp: Long
)
```

### 2.2 Implement Caching Strategy

**When Online**:
1. Fetch data from API
2. Save to Room database
3. Display from API response

**When Offline**:
1. Load from Room database
2. Show offline indicator
3. Display cached data

**Auto-Sync**:
```kotlin
fun syncDataIfNeeded() {
    viewModelScope.launch {
        if (isOnline && cacheIsStale()) {
            fetchFromApi()
        }
    }
}
```

### 2.3 Offline Indicator in TopBar

**File**: `ui/components/OfflineIndicator.kt`

```kotlin
@Composable
fun OfflineIndicator(
    isOnline: Boolean,
    onClick: () -> Unit
) {
    if (!isOnline) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
```

**Integration**: Add to TopAppBar in HomeScreen
**Click Action**: Show DisclaimerDialog with `showOfflineWarning = true`

---

## Phase 3: CSV Upload to API

### 3.1 Update UploadScreen Logic

**File**: `ui/screens/UploadScreen.kt`

**Current**: Saves CSV to local Room database
**New**: Upload to API, then save locally

**Flow**:
1. User selects CSV file
2. Show loading spinner
3. Upload to POST /datasets endpoint
4. On success:
   - Show success message with dataset_id
   - Save validation results
   - Navigate to home screen (or show new dataset)
5. On error:
   - Display validation errors
   - Allow retry

**Code**:
```kotlin
fun uploadCsvToApi(uri: Uri) {
    viewModelScope.launch {
        _uploadState.value = UiState.Loading

        // Convert URI to MultipartBody.Part
        val file = prepareFilePart(uri)

        repository.uploadDataset(file, nickname = "Mobile Upload")
            .onSuccess { response ->
                _uploadState.value = UiState.Success(response)
                // Also save to local DB for offline access
                saveToLocalDb(response.datasetId)
            }
            .onFailure { error ->
                _uploadState.value = UiState.Error(error.message ?: "Upload failed")
            }
    }
}
```

### 3.2 Add Upload Progress Indicator

Show progress during upload:
- LinearProgressIndicator (indeterminate)
- Or percentage if file size is tracked

---

## Phase 4: Settings Screen

### 4.1 Create SettingsScreen

**File**: `ui/screens/SettingsScreen.kt`

**Features**:
- **Dataset Selector**: Choose which dataset to display on home
- **Refresh Interval**: How often to fetch new data
- **Units**: mmol/L vs mg/dL (future)
- **Language**: For AI analysis (en, et, ru)
- **About**: App version, disclaimer link

**Example**:
```kotlin
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    Column {
        // Dataset Selection
        Text("Active Dataset")
        DatasetDropdown(
            datasets = viewModel.availableDatasets,
            selected = viewModel.activeDataset,
            onSelect = { viewModel.setActiveDataset(it) }
        )

        // Refresh Interval
        Text("Auto Refresh")
        Slider(
            value = viewModel.refreshInterval,
            onValueChange = { viewModel.setRefreshInterval(it) },
            valueRange = 5f..60f // minutes
        )
    }
}
```

### 4.2 Store Preferences in DataStore

**File**: `data/local/PreferencesRepository.kt`

```kotlin
class PreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    suspend fun setActiveDatasetId(id: String) {
        dataStore.edit { prefs ->
            prefs[ACTIVE_DATASET_KEY] = id
        }
    }

    fun getActiveDatasetId(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[ACTIVE_DATASET_KEY]
        }
    }

    companion object {
        private val ACTIVE_DATASET_KEY = stringPreferencesKey("active_dataset")
    }
}
```

---

## Phase 5: Polish and Optimization

### 5.1 Add Pull-to-Refresh

**File**: Update HomeScreen.kt

```kotlin
val refreshState = rememberSwipeRefreshState(isRefreshing)

SwipeRefresh(
    state = refreshState,
    onRefresh = { viewModel.refreshData() }
) {
    // Home screen content
}
```

### 5.2 Add Animations

- Smooth transitions between loading/success states
- Animated graph updates
- Status indicator pulse animation (for critical values)

### 5.3 Accessibility

- Content descriptions for all icons
- High contrast mode support
- Screen reader compatibility
- Haptic feedback for critical alerts

### 5.4 Performance Optimization

- Image caching (if adding profile pictures)
- Pagination for large datasets list
- Background data sync with WorkManager
- Memory leak prevention

---

## Implementation Order

### Week 1: Core Home Screen
1. ✅ MainViewModel with state management
2. ✅ Fetch latest dataset and analysis
3. ✅ Display current glucose + status
4. ✅ Make status clickable → Analysis screen

### Week 2: Graph and Timeframes
1. ✅ Glucose graph component (basic)
2. ✅ Timeframe selector (24h/7d/14d)
3. ✅ Graph click → Detailed metrics screen
4. ✅ Polish graph visuals

### Week 3: Offline Support
1. ✅ Room caching layer
2. ✅ Online/offline state detection
3. ✅ Offline indicator in TopBar
4. ✅ Auto-sync on reconnect

### Week 4: Upload & Settings
1. ✅ CSV upload to API
2. ✅ Settings screen with dataset selector
3. ✅ DataStore preferences
4. ✅ Pull-to-refresh

### Week 5: Polish
1. ✅ Animations and transitions
2. ✅ Accessibility improvements
3. ✅ Performance optimization
4. ✅ Testing and bug fixes

---

## Testing Checklist

### Unit Tests
- [ ] MainViewModel state updates
- [ ] Repository error handling
- [ ] Data mappers (DTO → Domain)
- [ ] Caching logic

### Integration Tests
- [ ] API calls with mock responses
- [ ] Room database operations
- [ ] ViewModel + Repository interaction

### UI Tests
- [ ] Home screen displays glucose correctly
- [ ] Status indicator opens analysis
- [ ] Graph click opens metrics
- [ ] Offline indicator shows/hides correctly
- [ ] CSV upload success/failure flows

### Manual Tests
- [ ] Fresh install experience
- [ ] Offline → Online transition
- [ ] Multiple dataset switching
- [ ] Error recovery (no internet, timeout)
- [ ] Different screen sizes/orientations

---

## Technical Debt to Address

1. **API Key Security**: Move to BuildConfig or secure storage
2. **Hardcoded Strings**: Finish migrating all to strings.xml
3. **Magic Numbers**: Extract to dimens.xml consistently
4. **Error Messages**: Localize for multiple languages
5. **Loading States**: Skeleton screens instead of spinners
6. **Code Comments**: Add KDoc to all public APIs

---

## Resources

### Libraries to Add
- **Vico**: `implementation("com.patrykandpatrick.vico:compose:1.13.0")`
- **WorkManager**: `implementation("androidx.work:work-runtime-ktx:2.9.0")`
- **DataStore**: Already added ✓
- **SwipeRefresh**: `implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")`

### Documentation
- [Vico Charts](https://github.com/patrykandpatrick/vico)
- [WorkManager Guide](https://developer.android.com/guide/background/persistent/getting-started)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [Compose Canvas](https://developer.android.com/jetpack/compose/graphics/draw/overview)

---

## Questions to Resolve

1. **Graph Library**: Vico vs MPAndroidChart vs Custom Canvas?
2. **Refresh Strategy**: Pull-to-refresh, auto-refresh, or manual button?
3. **Notification System**: Push notifications for critical glucose levels?
4. **Multi-User Support**: Needed for future?
5. **Data Export**: Allow exporting analysis reports as PDF or CSV?

---

## Success Criteria

✅ **Functional**:
- Home screen shows real-time glucose from API
- Offline mode works with cached data
- CSV upload creates new dataset on server
- Settings allow dataset switching

✅ **User Experience**:
- < 2 second load time for home screen
- Smooth animations and transitions
- Clear error messages with recovery options
- Intuitive navigation

✅ **Code Quality**:
- Clean architecture maintained
- Comprehensive test coverage (>70%)
- No memory leaks
- Follows Android best practices

---

**Last Updated**: October 28, 2025
**Status**: Planning Phase
**Target Completion**: Step 4 Milestone
