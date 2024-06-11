package com.dynamicadapter.lib.dao

import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
data class SectionInfo(val bodyList: ArrayList<HolderInfo>) {
    var sectionKey: Int = RecyclerView.NO_POSITION
    var indexInfo: IndexInfo = IndexInfo(RecyclerView.NO_POSITION)

    var headerInfo: HolderInfo? = null
    var footerInfo: HolderInfo? = null

    val size: Int
        get() {
            var cnt = bodyList.size
            if (headerInfo != null) {
                cnt += 1
            }

            if (footerInfo != null) {
                cnt += 1
            }
            return cnt
        }
    val isEmpty: Boolean
        get() = size == 0

    fun find(predicate: (HolderInfo) -> Boolean): HolderInfo? {
        headerInfo?.run {
            if (predicate(this)) {
                return headerInfo
            }
        }
        bodyList.forEach {
            if (predicate(it)) {
                return it
            }
        }
        footerInfo?.run {
            if (predicate(this)) {
                return footerInfo
            }
        }
        return null
    }

    fun isContainingData(data: Any?): Boolean {
        if (headerInfo?.data == data) {
            return true
        }
        if (bodyList.find { it.data == data } != null) {
            return true
        }
        if (footerInfo?.data == data) {
            return true
        }
        return false
    }

    fun applyAll(action: (HolderInfo) -> Unit) {
        headerInfo?.run(action)
        bodyList.forEach(action)
        footerInfo?.run(action)
    }

    fun clear() {
        headerInfo = null
        bodyList.clear()
        footerInfo = null
    }

    data class IndexInfo(val sectionIndex: Int)
}