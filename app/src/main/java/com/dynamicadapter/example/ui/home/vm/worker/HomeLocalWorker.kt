package com.dynamicadapter.example.ui.home.vm.worker

import com.dynamicadapter.example.ui.home.vm.process.HomeProcess
import com.dynamicadapter.lib.dao.HolderInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by 180842 on 2024/06/11.
 */
class HomeLocalWorker(private val scope: CoroutineScope) {
    private val process = HomeProcess()

    fun loadInit(onResult: (List<HolderInfo>) -> Unit) {
        scope.launch(Dispatchers.Default) {
            val result = process.process()

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }
}