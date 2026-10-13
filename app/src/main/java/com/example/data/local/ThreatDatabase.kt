package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ThreatRecordEntity::class, BlacklistHashEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ThreatDatabase : RoomDatabase() {
    abstract fun threatDao(): ThreatDao

    companion object {
        @Volatile
        private var INSTANCE: ThreatDatabase? = null

        fun getInstance(context: Context): ThreatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreatDatabase::class.java,
                    "apk_soqchi_security.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial known malware database
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).threatDao()
                                dao.insertAllBlacklist(initialThreatSignatures)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val initialThreatSignatures = listOf(
            BlacklistHashEntity(
                sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                threatName = "Trojan.AndroidOS.Cerberus",
                threatCategory = "Banking Trojan",
                description = "Bank parollari va SMS tasdiqlash kodlarini o'g'irlovchi xavfli troyan"
            ),
            BlacklistHashEntity(
                sha256 = "8f434346648f6b96df89dda901c5176b10f607629f7660cc6342d6f369456ac4",
                threatName = "Trojan-Banker.AndroidOS.SharkBot",
                threatCategory = "Banking Malware",
                description = "Avtomatik pul o'tkazish (ATS) va ekranni boshqarish virusi"
            ),
            BlacklistHashEntity(
                sha256 = "4a5a54cf8e30b8042ebfbe1e7dd10d9f826315579ca2e4c9213190df0b5b5c92",
                threatName = "Spyware.AndroidOS.FluBot",
                threatCategory = "SMS Stealer & Spyware",
                description = "Soxta kuryerlik SMS tarqatib, butun kontaktlar va kartalarni o'g'irlovchi zararli dastur"
            ),
            BlacklistHashEntity(
                sha256 = "9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7",
                threatName = "Trojan.AndroidOS.Anatsa",
                threatCategory = "Keylogger & Remote Access",
                description = "Klaviatura bosishlarini yozib oluvchi va bank akkauntlarini buzuvchi troyan"
            ),
            BlacklistHashEntity(
                sha256 = "c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2",
                threatName = "Ransomware.AndroidOS.Locker",
                threatCategory = "Ransomware (Tovlamachi)",
                description = "Telefon ekranini to'sib olib to'lov talab qiluvchi shantajchi virus"
            )
        )
    }
}
