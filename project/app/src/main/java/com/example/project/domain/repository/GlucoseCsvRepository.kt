package com.example.project.domain.repository

import android.content.Context
import android.net.Uri
import com.example.project.domain.model.GlucoseReading

interface GlucoseCsvRepository {
    fun parseGlucoseData(context: Context, uri: Uri): List<GlucoseReading>
}
