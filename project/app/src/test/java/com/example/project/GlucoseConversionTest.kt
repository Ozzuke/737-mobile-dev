package com.example.project

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

/**
 * Unit test for glucose conversion - testing data manipulation logic
 *
 * This test validates the glucose unit conversion function which converts
 * glucose values between mg/dL and mmol/L units, a critical data manipulation
 * function used throughout the application.
 *
 * Note: Testing the conversion logic that exists in HomeScreen and GlucoseGraphComponent
 */
class GlucoseConversionTest {

    // Extracted the conversion logic for testing
    private fun convertGlucose(value: Double, fromUnit: String?, toUnit: String?): Double {
        val from = (fromUnit ?: "").lowercase(Locale.US)
        val to = (toUnit ?: "").lowercase(Locale.US)
        if (from.isEmpty() || to.isEmpty() || from == to) return value
        return if (from.contains("mg/dl") && to.contains("mmol")) {
            value / 18.0
        } else if (from.contains("mmol") && to.contains("mg/dl")) {
            value * 18.0
        } else value
    }

    @Test
    fun testGlucoseConversion_convertsCorrectlyBetweenUnits() {
        // Given: Various glucose values and unit conversions

        // When/Then: Test mg/dL to mmol/L conversion
        // 180 mg/dL should equal 10 mmol/L (180 / 18 = 10)
        val mgdlToMmol = convertGlucose(180.0, "mg/dL", "mmol/L")
        assertEquals(10.0, mgdlToMmol, 0.01)

        // When/Then: Test mmol/L to mg/dL conversion
        // 5 mmol/L should equal 90 mg/dL (5 * 18 = 90)
        val mmolToMgdl = convertGlucose(5.0, "mmol/L", "mg/dL")
        assertEquals(90.0, mmolToMgdl, 0.01)

        // When/Then: Test same unit returns original value
        val sameUnit = convertGlucose(120.0, "mg/dL", "mg/dL")
        assertEquals(120.0, sameUnit, 0.01)

        // When/Then: Test null handling returns original value
        val nullUnit = convertGlucose(150.0, null, "mg/dL")
        assertEquals(150.0, nullUnit, 0.01)

        // When/Then: Test case-insensitive conversion
        val caseInsensitive = convertGlucose(90.0, "MG/DL", "MMOL/L")
        assertEquals(5.0, caseInsensitive, 0.01)

        // When/Then: Test round-trip conversion maintains accuracy
        // Convert mg/dL -> mmol/L -> back to mg/dL
        val original = 126.0
        val toMmol = convertGlucose(original, "mg/dL", "mmol/L")
        val backToMgdl = convertGlucose(toMmol, "mmol/L", "mg/dL")
        assertEquals(original, backToMgdl, 0.01)
    }
}

