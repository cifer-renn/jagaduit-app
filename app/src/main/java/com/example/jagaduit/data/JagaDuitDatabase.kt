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
            // Data Awal: Expense Categories
            dao.insertCategory(CategoryEntity(name = "Makanan", type = "EXPENSE"))
            dao.insertCategory(CategoryEntity(name = "Transport", type = "EXPENSE"))
            dao.insertCategory(CategoryEntity(name = "Belanja", type = "EXPENSE"))
            dao.insertCategory(CategoryEntity(name = "Tagihan", type = "EXPENSE"))

            // Data Awal: Income Categories
            dao.insertCategory(CategoryEntity(name = "Gaji", type = "INCOME"))
            dao.insertCategory(CategoryEntity(name = "Bonus", type = "INCOME"))

            // Data Awal: Default Accounts (Dompet)
            // Pastikan method insertAccount ada di AppDao.
            // Kalau error merah disini, berarti pindahkan insertAccount ke AccountDao.
            dao.insertAccount(AccountEntity(name = "Cash", balance = 0.0))
            dao.insertAccount(AccountEntity(name = "BCA", balance = 0.0))
            dao.insertAccount(AccountEntity(name = "Gopay", balance = 0.0))
        }
    }
}