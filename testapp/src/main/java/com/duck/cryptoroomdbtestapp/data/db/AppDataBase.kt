package com.duck.cryptoroomdbtestapp.data.db

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Room
import androidx.room.TypeConverters
import com.duck.cryptoroomdb.CryptoRoomDatabase
import com.duck.cryptoroomdb.typeconverter.CryptoStringTypeConverter
import com.duck.cryptoroomdbtestapp.data.db.dao.UserDao
import com.duck.cryptoroomdbtestapp.data.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
    ],
    version = AppDatabase.DATABASE_VERSION,
)
@TypeConverters(
    CryptoStringTypeConverter::class,
)
@RewriteQueriesToDropUnusedColumns
abstract class AppDatabase : CryptoRoomDatabase() {

    abstract val userDao: UserDao

    companion object {
        const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: AppDatabase? = null

        @VisibleForTesting
        private val DATABASE_NAME = "crypto-test-db"

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also {
                    INSTANCE = it
                }
            }

        /**
         * Set up the database configuration.
         * The SQLite database is only created when it's accessed for the first time.
         */
        private fun buildDatabase(appContext: Context): AppDatabase {
            return Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration(false)
                .build().apply {
                    val cryptor = Cryptor(appContext)
                    setCryptoHelpers(cryptor, cryptor)
                }
        }
    }
}