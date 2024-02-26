package com.dearmyhealth.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dearmyhealth.data.db.dao.AlarmDao
import com.dearmyhealth.data.db.dao.DietDao
import com.dearmyhealth.data.db.dao.DosageDao
import com.dearmyhealth.data.db.dao.FoodDao
import com.dearmyhealth.data.db.dao.MedicationDao
import com.dearmyhealth.data.db.dao.NutrientStandardDao
import com.dearmyhealth.data.db.dao.UserDao
import com.dearmyhealth.data.db.entities.Alarm
import com.dearmyhealth.data.db.entities.Diet
import com.dearmyhealth.data.db.entities.Dosage
import com.dearmyhealth.data.db.entities.EAR
import com.dearmyhealth.data.db.entities.Food
import com.dearmyhealth.data.db.entities.Goal
import com.dearmyhealth.data.db.entities.Medication
import com.dearmyhealth.data.db.entities.RNI
import com.dearmyhealth.data.db.entities.Symptom
import com.dearmyhealth.data.db.entities.User

@Database(
    entities = [
        User::class,
        Food::class,
        Diet::class,
        EAR::class,
        RNI::class,
        Medication::class,
        Dosage::class,
        Alarm::class,
        Goal::class,
        Symptom::class,], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun dietDao(): DietDao
    abstract fun dosageDao(): DosageDao
    abstract fun medicationDao(): MedicationDao
    abstract fun alarmDao(): AlarmDao
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
                    .createFromAsset("food.db")
                    .createFromAsset("standard.db")
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}