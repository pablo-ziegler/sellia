package com.example.selliaapp.data.local.converters

import androidx.room.TypeConverter
import com.example.selliaapp.data.local.entity.ReportScope

class ReportConverters {
    @TypeConverter
    fun scopeToString(scope: ReportScope): String = scope.name

    @TypeConverter
    fun stringToScope(value: String): ReportScope = ReportScope.valueOf(value)
}
