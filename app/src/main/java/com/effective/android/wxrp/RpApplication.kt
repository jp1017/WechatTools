package com.effective.android.wxrp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.effective.android.wxrp.store.Config
import com.effective.android.wxrp.store.db.PacketRecordDataBase
import com.effective.android.wxrp.store.db.PacketRepository

class RpApplication : Application() {

    companion object {

        private const val SP_FILE_NAME = "sp_name_wxrp"

        @Volatile
        private var instance: Application? = null
        var sharedPreferences: SharedPreferences? = null
        var packetRepository: PacketRepository? = null
        var database: PacketRecordDataBase? = null

        @Synchronized
        fun INSTANCE(): Application {
            return instance!!
        }

        fun SP(): SharedPreferences {
            return sharedPreferences!!
        }

        fun PACKET_REPOSITORY(): PacketRepository {
            return packetRepository!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        sharedPreferences = getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)
        database = PacketRecordDataBase.getInstance(this)
        packetRepository = PacketRepository(database!!.packetRecordDao())
        Config.init()
    }

    override fun onTerminate() {
        super.onTerminate()
        Config.onSave()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Config.onSave()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Config.onSave()
    }
}