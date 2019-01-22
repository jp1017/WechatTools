package com.effective.android.wxrp

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.effective.android.wxrp.store.db.PacketRecord
import com.effective.android.wxrp.store.db.PacketRepository
import com.effective.android.wxrp.utils.singleArgViewModelFactory
import kotlinx.coroutines.*


class MainViewModel(private val repository: PacketRepository) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val _all_data = MutableLiveData<List<PacketRecord>>()
    val _add_data = MutableLiveData<PacketRecord>()

    companion object {
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun inserPacket(packetRecord: PacketRecord): Job {
        return uiScope.launch {
            val packetRecord = async { repository.insertPacket(packetRecord) }
            _add_data.value = packetRecord.await()
        }
    }

    fun loadPacketList() {
        uiScope.launch {
            val packetRecords = async { repository.getPacketList() }
            _all_data.value = packetRecords.await()
        }
    }
}