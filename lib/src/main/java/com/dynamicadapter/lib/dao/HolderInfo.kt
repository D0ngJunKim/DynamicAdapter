package com.dynamicadapter.lib.dao

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dynamicadapter.lib.holder.GlobalViewHolder

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
class HolderInfo(val clazz: Class<out GlobalViewHolder>,
                 val data: Any?,
                 var spanCount: Int = GridLayoutManager.DEFAULT_SPAN_COUNT) {
    // ViewType은 자동 생성
    var viewType: Int = RecyclerView.NO_POSITION
    // Sticky 대상 여부
    var isSticky: Boolean = false

    var extraData: Any? = null
    var isDataChanged: Boolean = false

    var indexInfo: IndexInfo = IndexInfo(RecyclerView.NO_POSITION, RecyclerView.NO_POSITION, RecyclerView.NO_POSITION)


    data class IndexInfo(val sectionKey: Int,
                         val sectionIndex: Int,
                         val itemIndex: Int)
}