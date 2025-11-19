package com.example.project

import com.example.project.ui.screens.convertGlucose
import org.junit.Assert.*
import org.junit.Test

/**
 * This test validates the glucose unit conversion function which converts
 * glucose values between mg/dL and mmol/L units
 */
class GlucoseConversionTest {


    @Test
    fun testGlucoseConversion_convertsCorrectlyBetweenUnits() {

        val mgdlToMmol = convertGlucose(180.0, "mg/dL", "mmol/L")
        assertEquals(10.0, mgdlToMmol, 0.01)

        val mmolToMgdl = convertGlucose(5.0, "mmol/L", "mg/dL")
        assertEquals(90.0, mmolToMgdl, 0.01)

        val sameUnit = convertGlucose(120.0, "mg/dL", "mg/dL")
        assertEquals(120.0, sameUnit, 0.01)

        val nullUnit = convertGlucose(150.0, null, "mg/dL")
        assertEquals(150.0, nullUnit, 0.01)

        val caseInsensitive = convertGlucose(90.0, "MG/DL", "MMOL/L")
        assertEquals(5.0, caseInsensitive, 0.01)

        val original = 126.0
        val toMmol = convertGlucose(original, "mg/dL", "mmol/L")
        val backToMgdl = convertGlucose(toMmol, "mmol/L", "mg/dL")
        assertEquals(original, backToMgdl, 0.01)
    }
}

