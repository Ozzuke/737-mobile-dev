## Which API was chosen and why

* **API:** Custom CGM Analyzer API at `http://cgm.cloud.ut.ee/api/v1`
* **Authentication:** API Key via `X-API-Key` header
* **Documentation:** OpenAPI 3.0 spec (`docs/backend_openapi_v1.yaml`)
* **Rationale:** Custom backend provides exact data structures needed (datasets, glucose overlays, AI analysis); demonstrates full-stack integration

---

## Example API endpoint used

### GET /datasets
* **Purpose:** List all uploaded CGM datasets
* **Response:** Array of datasets with `dataset_id`, `nickname`, date range, row count, sampling interval
* **UI usage:** `DatasetsScreen` displays list; clicking opens analysis

### POST /analyze
* **Purpose:** Get AI-powered glucose analysis for a dataset
* **Request:** `dataset_id`, `preset` (24h/7d/14d), `lang`
* **Response:** Overall rating, detected patterns, trends, extrema, AI summary
* **UI usage:** `AnalysisScreen` shows comprehensive analysis with color-coded severity

### Other endpoints
* `GET /healthz` - API availability check
* `GET /datasets/{id}?preset={preset}` - Glucose overlay data for charts
* `POST /datasets` - CSV upload (multipart/form-data)

---

## Error handling strategy

### Network-level errors
* **No internet:** Catches `UnknownHostException`, displays "No internet connection" with retry button
* **Timeout:** OkHttp 30-second timeout, shows "Request timeout" message
* **UI state pattern:** Sealed `UiState<T>` class with `Idle`, `Loading`, `Success`, `Error`, `Empty` states
* **Loading:** `CircularProgressIndicator` with descriptive message
* **Error:** Red card with user-friendly message + retry button
* **No crashes:** All exceptions caught in repository, converted to `UiState.Error`

### HTTP status codes
* **401:** "Authentication failed. Please check your API key."
* **404:** "Resource not found."
* **500/503:** "Server error. Please try again later."

---

## Technical implementation

### Architecture layers
1. **DTOs** (`data/remote/dto/`): Match OpenAPI spec with `@JsonClass` for Moshi code generation
2. **Domain Models** (`domain/model/`): UI-friendly representations with enums
3. **Mappers** (`data/remote/mapper/`): Extension functions converting DTO → Domain
4. **Retrofit Service** (`data/remote/api/CgmApiService.kt`): Interface with suspend functions
5. **API Key Interceptor** (`data/remote/interceptor/ApiKeyInterceptor.kt`): Adds `X-API-Key` header to all requests
6. **Repository Interface** (`domain/repository/CgmApiRepository.kt`): Abstraction for testability
7. **Repository Impl** (`data/remote/repository/CgmApiRepositoryImpl.kt`): Executes API calls with `withContext(Dispatchers.IO)`, maps DTOs, wraps in `Result<T>`, converts HTTP errors to user messages
8. **ViewModel** (`ui/viewmodels/CgmApiViewModel.kt`): Manages `StateFlow<UiState<T>>`, launches coroutines with `viewModelScope`
9. **Screens** (`ui/screens/`): Observes ViewModel state with `collectAsState()`, renders Loading/Error/Success/Empty states

### Dependencies added
```kotlin
// Retrofit + OkHttp + Moshi
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.squareup.moshi:moshi:1.15.1")
implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
```

### Dependency injection
* **Application class** (`CGMApplication.kt`): Provides singleton repositories
* **ViewModel factories:** Inject repositories into ViewModels
* **Compose:** Retrieves Application instance, creates ViewModels with factories

---

## Challenges and solutions

* **Type-safe JSON parsing with nested structures**
  * *Challenge:* OpenAPI spec has deeply nested responses; manual parsing error-prone
  * *Solution:* Moshi with Kotlin code generation (`@JsonClass(generateAdapter = true)`); created comprehensive DTO hierarchy matching spec exactly

* **Cleartext HTTP communication**
  * *Challenge:* Android blocks HTTP by default
  * *Solution:* Created `network_security_config.xml` allowing cleartext for `cgm.cloud.ut.ee` only

* **User-friendly error messages**
  * *Challenge:* Raw exceptions (`SocketTimeoutException`, `UnknownHostException`) are technical
  * *Solution:* Error mapping in ViewModel checks exception type; converts to plain language ("No internet connection", "Request timeout")

* **Loading states during async operations**
  * *Challenge:* User sees blank screen while data fetches
  * *Solution:* Implemented `UiState` sealed class with `Loading` state; UI renders `CircularProgressIndicator` with descriptive message

* **Repository abstraction vs direct API calls**
  * *Challenge:* ViewModels directly calling Retrofit couples UI to network implementation
  * *Solution:* Repository pattern with interface in domain layer; enables mock repositories for unit tests

* **Shared composables for state views**
  * *Challenge:* Loading/error/empty states duplicated across screens
  * *Solution:* Extracted `LoadingView`, `ErrorView`, `EmptyView` to `ui/components/StateViews.kt`; reusable across screens

* **Analysis screen preset switching**
  * *Challenge:* User wants to see 24h/7d/14d analysis without navigating away
  * *Solution:* Local state in `AnalysisScreen` for selected preset; `LaunchedEffect` re-triggers API call when preset changes

* **Educational disclaimer compliance**
  * *Challenge:* App provides medical data but must clarify it's not medical advice
  * *Solution:* Created `DisclaimerDialog` shown on app launch; blocks usage until acknowledged

---

## Screenshots / evidence

### Datasets Screen
* **Loading:** Circular progress with "Loading datasets..." message
* **Success:** List of dataset cards (nickname, dates, row count, interval)
* **Error:** Red error card with user-friendly message + retry button
* **Empty:** "No datasets available" centered text

### Analysis Screen
* **Overall Rating Card:** Color-coded by category (green=GOOD, yellow=ATTENTION, red=URGENT), score, reasons
* **Patterns Card:** Pattern name, severity badge, summary, instance count
* **Trends Card:** Direction (UP/DOWN), time span, slope
* **Extrema Card:** MIN/MAX with glucose value, unit, time
* **Preset Selector:** Three `FilterChip` buttons (24h/7d/14d)

### Disclaimer Dialog
* **Icon:** Large warning icon
* **Title:** "Important Disclaimer"
* **Sections:** Educational Use Only, Not Medical Advice, Consult Healthcare Professionals, No Liability
* **Button:** "I Understand"
* **Shown on app launch**
