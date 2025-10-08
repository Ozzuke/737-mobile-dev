## What data is stored locally

* **Entity/Table:** `glucose_readings` (Room) with fields

  * `id: Long` (auto-generated primary key)
  * `timestamp: String` (as read from CSV)
  * `glucoseValue: Double` 
* **Write path:** CSV to in-memory `List<GlucoseReading>` to bulk insert with `OnConflictStrategy.REPLACE` via `GlucoseDao.insertAll(readings)`; Upload flow triggers `viewModel.updateReadings(readings)` after parsing.
* **Read path (for UI):**

  * All readings as `Flow<List<GlucoseReading>>`, ordered by `timestamp DESC` for Home/dashboard. 
  * Latest reading as `Flow<GlucoseReading?>` (used by `GlucoseWidget`).
* **Management:** Ability to wipe all rows via `deleteAll()`. 

---

## Challenges and solutions

* **Parsing real CSV exports (noise & mixed rows)**

  * *Challenge:* Files include metadata/header rows and multiple event types.
  * *Solution:* Parser filters to lines where **Event Type == "EGV"**, extracts timestamp and value, and guards against `NumberFormatException`. 

* **User feedback during imports**

  * *Challenge:* Communicate upload progress and errors.
  * *Solution:* `UploadScreen` uses a `CircularProgressIndicator`, status `Card`, and shows success/error messages after parsing and insert (*validation*). 

* **Efficient persistence & fresh UI**

  * *Challenge:* Insert many readings and reflect changes immediately on the Home screen.
  * *Solution:* Bulk insert with Room DAO; repository exposes `Flow`s; `GlucoseViewModel` converts to `StateFlow` for Compose (`stateIn`), keeping UI reactive and efficient.

* **Empty/first-run state**

  * *Challenge:* No data present before first upload.
  * *Solution:* `GlucoseWidget` renders a clear “No Data / Upload CSV” prompt when `latestReading` is null. 

* **Timestamp handling & display**

  * *Challenge:* Timestamps are stored as `String`; sorting/formatting must still look correct.
  * *Current approach:* SQL orders by `timestamp DESC`; widget trims the displayed time with simple substring logic. *(Future improvement: migrate to epoch/`Instant` for robust ordering/timezone handling.)*

* **Resetting data**

  * *Challenge:* Provide a quick way to clear corrupted or test data.
  * *Solution:* DAO exposes `deleteAll()`; ViewModel wraps it in `deleteAllReadings()`.


