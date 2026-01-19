package com.example.jagaduit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, AccountEntity::class],
    version = 1,
    exportSchema = false
)
abstract class JagaDuitDatabase : RoomDatabase() {

    // KITA BUTUH KEDUANYA:
    // 1. AppDao untuk Transaksi (sesuai kodingan lama)
    abstract fun appDao(): AppDao

    // 2. AccountDao untuk ViewModel Account (sesuai kodingan baru)
    abstract fun accountDao(): AccountDao

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
                    .fallbackToDestructiveMigration() // Tambahan aman: Reset db jika struktur berubah drastis
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Callback untuk mengisi data awal saat database pertama kali dibuat
    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Kita pakai appDao untuk isi data awal
                    // (Asumsi function insertCategory & insertAccount ada di AppDao)
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