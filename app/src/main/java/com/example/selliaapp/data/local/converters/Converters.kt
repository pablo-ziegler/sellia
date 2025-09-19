package com.example.selliaapp.data.local.converters


import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Converters de Room para tipos java.time.
 * NO es @Entity. NO va en la lista de entities de @Database.
 * Se usa sólo en @TypeConverters(Converters::class) del AppDatabase.
 */
object Converters {


    // ----- LocalDate -----
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? =
        value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? =
        date?.toEpochDay()

    // ----- Instant -----
    @TypeConverter
    fun fromEpochMillis(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun instantToEpochMillis(instant: Instant?): Long? =
        instant?.toEpochMilli()

    // ----- LocalDateTime -----
    @TypeConverter
    fun fromEpochMillisToLdt(value: Long?): LocalDateTime? =
        value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }

    @TypeConverter
    fun ldtToEpochMillis(ldt: LocalDateTime?): Long? =
        ldt?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
}
