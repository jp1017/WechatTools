package com.effective.android.wxrp.store.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

@Dao
interface PacketRecordDao {

    @Query("SELECT * from packetRecord")
    fun getAll(): List<PacketRecord>

    @Insert(onConflict = REPLACE)
    fun insert(packetRecord: PacketRecord)
}