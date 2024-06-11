package com.dynamicadapter.example.ui.home.vm.process

import com.dynamicadapter.example.ui.home.unit.full.FullHolder
import com.dynamicadapter.example.ui.home.unit.full.getFullUiData
import com.dynamicadapter.example.ui.home.unit.span.SpanHolder
import com.dynamicadapter.example.ui.home.unit.span.getSpanUiData
import com.dynamicadapter.lib.dao.HolderInfo
import kotlin.random.Random

/**
 * Created by Dong Jun Kim on 2024/06/11.
 */
class HomeProcess {
    fun process(): ArrayList<HolderInfo> {
        val holderList = arrayListOf<HolderInfo>()

        for (i in 0 until 80) {
            val remainder = Random.nextInt(6, 10)
            if (i % remainder == 0) {
                holderList.add(HolderInfo(FullHolder::class.java, getFullUiData()).apply {
                    isSticky = true
                })
            } else {
                holderList.add(HolderInfo(SpanHolder::class.java, getSpanUiData(), 1))
            }
        }

        return holderList
    }
}