package com.effective.android.wxrp.viewmodel

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

    companion object {
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun loadPacketList() {
        uiScope.launch (Dispatchers.Main + viewModelJob){
            val packetRecords = async(Dispatchers.IO) {
                return@async repository.getPacketList() }.await()

            _all_data.value = packetRecords
        }
    }
}