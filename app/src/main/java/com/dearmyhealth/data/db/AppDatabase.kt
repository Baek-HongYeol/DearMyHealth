package com.dearmyhealth.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dearmyhealth.data.db.dao.*
import com.dearmyhealth.data.db.entities.*
import com.dearmyhealth.data.db.preload.PreloadMedicationsCallback
import com.dearmyhealth.data.db.preload.PreloadTestUserCallback
import com.dearmyhealth.data.db.views.DosageAlarm

@Database(
    entities = [
        User::class,
        Food::class,
        Diet::class,
        EAR::class,
        RNI::class,
        Medication::class,
        AttentionDetail::class,
        Dosage::class,
        Alarm::class,
        Goal::class,
        Symptom::class,],
    views =[DosageAlarm::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun dietDao(): DietDao
    abstract fun dosageDao(): DosageDao
    abstract fun medicationDao(): MedicationDao
    abstract fun attentionDetailDao(): AttentionDetailDao
    abstract fun alarmDao(): AlarmDao
    abstract fun dosageAlarmDao(): DosageAlarmDao
    abstract fun nutrientStandardDao(): NutrientStandardDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .createFromAsset("preload.db")
                    .addCallback(PreloadMedicationsCallback(context))
                    .addCallback(PreloadTestUserCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}