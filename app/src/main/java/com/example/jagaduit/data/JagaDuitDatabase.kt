package com.example.jagaduit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// --- PERHATIKAN BAGIAN ENTITIES & VERSION ---
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        GoalEntity::class,
        GoalHistoryEntity::class // <--- WAJIB DITAMBAHKAN (Ini yang bikin error)
    ],
    version = 4, // <--- NAIKKAN VERSI JADI 4
    exportSchema = false
)
abstract class JagaDuitDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao
    abstract fun accountDao(): AccountDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: JagaDuitDatabase? = null

        fun getDatabase(context: Context): JagaDuitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JagaDuitDatabase::class.java,
                    "jagaduit_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // Reset DB kalau struktur berubah
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.appDao())
                }
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            // Data awal kategori & akun (sama seperti sebelumnya)
            dao.insertCategory(CategoryEntity(name = "Makanan", type = "EXPENSE"))
            dao.insertCategory(CategoryEntity(name = "Transport", type = "EXPENSE"))
            dao.insertCategory(CategoryEntity(name = "Gaji", type = "INCOME"))

            dao.insertAccount(AccountEntity(name = "Cash", balance = 0.0))
            dao.insertAccount(AccountEntity(name = "BCA", balance = 0.0))
        }
    }
}