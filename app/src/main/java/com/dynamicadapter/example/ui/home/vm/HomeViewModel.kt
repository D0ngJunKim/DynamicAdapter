package com.dynamicadapter.example.ui.home.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamicadapter.example.ui.home.unit.full.FullUiData
import com.dynamicadapter.example.ui.home.vm.worker.HomeLocalWorker
import com.dynamicadapter.lib.dao.HolderInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    private val _dataList = MutableLiveData<Pair<List<HolderInfo>, Runnable?>>()
    val dataList: LiveData<Pair<List<HolderInfo>, Runnable?>> get() = _dataList

    private val localWorker = HomeLocalWorker(viewModelScope)

    fun loadInit() {
        localWorker.loadInit { holderList ->
            _dataList.value = holderList to null
        }
    }

    fun onChangeOrientation(isVertical: Boolean, runnable: Runnable) {
        val dataList = _dataList.value?.first?.toMutableList() ?: return
        viewModelScope.launch(Dispatchers.Default) {
            var data: FullUiData
            for (holderInfo in dataList) {
                data = holderInfo.data as? FullUiData ?: continue
                data.isVertical = isVertical
                holderInfo.isDataChanged = true
            }

            withContext(Dispatchers.Main) {
                _dataList.value = dataList to runnable
            }
        }
    }
}