package com.campuspulse.campuspulse

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Event::class, Ticket::class], version = 2, exportSchema = false)
abstract class CampusPulseDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: CampusPulseDatabase? = null

        fun getDatabase(context: Context): CampusPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CampusPulseDatabase::class.java,
                    "campus_pulse_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val eventDao = getDatabase(context).eventDao()
                eventDao.insertEvents(EventRepository.DEFAULT_EVENTS)
            }
        }
    }
}