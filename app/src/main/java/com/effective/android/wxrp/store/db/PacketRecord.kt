package com.effective.android.wxrp.store.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "packetRecord")
data class PacketRecord(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        @ColumnInfo(name = "post_user") var postUser: String = "",
        @ColumnInfo(name = "time") var time: Long = 0,
        @ColumnInfo(name = "number") var num: Float = 0.0f) {
}