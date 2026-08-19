package com.assetsalert.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromAssetType(v: AssetType): String = v.name
    @TypeConverter
    fun toAssetType(v: String): AssetType = AssetType.valueOf(v)

    @TypeConverter
    fun fromDirection(v: Direction): String = v.name
    @TypeConverter
    fun toDirection(v: String): Direction = Direction.valueOf(v)
}

@Database(entities = [Alert::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "assets_alert.db"
                ).build().also { INSTANCE = it }
            }
    }
}
