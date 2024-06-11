package com.dynamicadapter.lib.holder

import android.view.ViewGroup
import android.widget.Space
import com.dynamicadapter.lib.dao.HolderInfo

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
class ExceptionHolder(parent: ViewGroup, property: CommonProperty) :
        GlobalViewHolder(Space(parent.context), property) {
    init {
        itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
    }

    override fun setData(holderInfo: HolderInfo, position: Int) {
        // No Op.
    }
}