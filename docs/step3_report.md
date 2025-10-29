# Step 3: API Integration Report

## API Selection & Endpoints

* **Chosen API:** CGM Analyzer API (`http://cgm.cloud.ut.ee/api/v1`)
* **Rationale:** Domain-relevant medical data API with complex JSON structures, multiple CRUD operations, authentication requirements, and AI integration - demonstrates real-world production API patterns.
* **Key Endpoints Used:**
  * `GET /healthz` - Health check
  * `GET /datasets` - List all datasets with metadata
  * `GET /datasets/{id}?preset=24h|7d|14d` - Fetch glucose data points for visualization
  * `POST /analyze` - Rule-based pattern detection (trends, extrema, patterns)
  * `POST /analyze/explain` - LLM-powered insights with recommendations
  * `POST /datasets` - Multipart file upload (CSV)
  * `DELETE /datasets/{id}` - Remove dataset
* **Authentication:** API key via `X-API-Key` header, injected automatically by `ApiKeyInterceptor`.

---

## Technical Implementation

* **HTTP Client:** Retrofit 2.9.0 with Moshi converter for JSON serialization/deserialization
* **Async Handling:** Kotlin Coroutines (`suspend` functions) with `Dispatchers.IO` for network operations
* **Architecture:** Repository pattern - `UI (Compose) → ViewModel → Repository → Retrofit → API`
* **State Management:** `StateFlow<UiState<T>>` with sealed class (`Idle`, `Loading`, `Success`, `Error`, `Empty`)
* **Data Models:**
  * DTOs (`@JsonClass(generateAdapter = true)`) with `@Json(name = "...")` for API layer
  * Domain models for business logic layer
  * Mapper functions convert DTOs to domain models in Repository
* **Network Configuration:**
  * `LoggingInterceptor` for debugging (logs full request/response)
  * `ApiKeyInterceptor` for authentication
  * 30-second timeouts for connect/read operations

---

## Challenges and Solutions

* **Complex nested JSON structures**
  * *Challenge:* API returns deeply nested responses (e.g., `AnalyzeResponseDto` with `annotations.trends[]`, `patterns[]`, `meta`, `overall`).
  * *Solution:* Created hierarchical DTO structure with Moshi code generation; separate data classes for each nesting level; mapper functions flatten to simpler domain models for UI consumption.

* **Error handling without crashes**
  * *Challenge:* Network failures, HTTP errors, timeouts, and missing API keys must not crash the app.
  * *Solution:* Repository wraps all calls in `try-catch`, returns `Result<T>`; specific detection for `UnknownHostException` (no internet), timeout messages, and HTTP status codes (401, 404, 422, 500, 503); ViewModel converts to `UiState.Error` with user-friendly messages; all error states include retry buttons.

* **Multi-endpoint orchestration**
  * *Challenge:* Home screen needs data from 3 endpoints (`getDatasets` → `getDatasetData` → `analyzeDataset`) to display complete view.
  * *Solution:* `MainViewModel.fetchLatestData()` chains calls sequentially using coroutine `onSuccess` blocks; updates individual state properties as each call completes; UI observes combined state object.

* **Loading state feedback**
  * *Challenge:* Users need to know when data is being fetched vs when it's ready.
  * *Solution:* Every screen implements `UiState.Loading` with contextual messages ("Loading datasets...", "Analyzing...", "Generating AI insights..."); `CircularProgressIndicator` with text labels; state transitions are immediate and visible.

* **Authentication management**
  * *Challenge:* API key must be included in every request without manual repetition.
  * *Solution:* `ApiKeyInterceptor` implements `Interceptor.intercept()`, reads key from storage, adds `X-API-Key` header to all requests automatically; centralized auth logic prevents forgetting keys on new endpoints.

* **Real-time data visualization**
  * *Challenge:* Display glucose graph with proper axes, grid lines, and color-coded ranges from API data.
  * *Solution:* `DatasetDataResponseDto` provides `overlay.days[].points[]` with minute-of-day timestamps; UI calculates Y-axis ticks dynamically based on glucose range; horizontal grid lines drawn at tick intervals; points colored by glucose value (low/in-range/high).

---

## Error Handling Strategy

* **Network Errors:**
  * `UnknownHostException` → "No internet connection. Please check your network."
  * Timeout → "Request timeout. Please try again."
  * Generic network failures → "Network error occurred."

* **HTTP Status Codes:**
  * `401 Unauthorized` → "Authentication failed. Please check your API key."
  * `404 Not Found` → "Resource not found."
  * `422 Unprocessable Entity` → "Validation error: [error body details]"
  * `500 Internal Server Error` → "Server error. Please try again later."
  * `503 Service Unavailable` → "Service unavailable. Please try again later."

* **Recovery Mechanisms:**
  * All error states include retry buttons
  * Connectivity check before critical operations
  * Graceful degradation (e.g., show analysis without LLM explanation if 503)
  * No app crashes - all exceptions caught and converted to user-friendly messages

---

## Example API Call Flow

**User opens Datasets screen:**
1. UI calls `viewModel.fetchDatasets()`
2. ViewModel sets `_datasetsState.value = UiState.Loading`
3. Repository calls `apiService.getDatasets()` (Retrofit)
4. `ApiKeyInterceptor` adds `X-API-Key` header
5. Backend responds with JSON: `{ "items": [...] }`
6. Moshi parses JSON to `DatasetsResponseDto`
7. Repository maps DTOs to `List<DatasetSummary>`, returns `Result.success(datasets)`
8. ViewModel updates `_datasetsState.value = UiState.Success(datasets)`
9. UI observes StateFlow change, recomposes to show dataset list

**If network error at step 5:**
- Repository catches exception, returns `Result.failure(error)`
- ViewModel updates `_datasetsState.value = UiState.Error("No internet connection")`
- UI shows error view with retry button
- User clicks retry → back to step 1
