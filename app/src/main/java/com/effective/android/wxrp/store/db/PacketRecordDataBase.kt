package com.effective.android.wxrp.store.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(PacketRecord::class), version = 1)
abstract class PacketRecordDataBase : RoomDatabase() {

    abstract fun packetRecordDao(): PacketRecordDao

    companion object {
        private var INSTANCE: PacketRecordDataBase? = null

        fun getInstance(context: Context): PacketRecordDataBase {
            if (INSTANCE == null) {
                synchronized(PacketRecordDataBase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            PacketRecordDataBase::class.java, "packetRecord.db").build()
                }
            }
            return INSTANCE!!
        }

        fun destory() {
            INSTANCE = null
        }
    }
}