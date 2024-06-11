package com.dynamicadapter.lib.holder

import androidx.viewbinding.ViewBinding
import com.dynamicadapter.lib.dao.HolderInfo

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
abstract class GlobalBindingViewHolder<VDB : ViewBinding, DATA : Any?>(protected val vBinding: VDB,
                                                                       property: CommonProperty) : GlobalViewHolder(vBinding.root, property) {

    abstract fun onDataChanged(data: DATA, position: Int)
    open fun onDataDuplicated(data: DATA, position: Int) {}

    final override fun setData(holderInfo: HolderInfo, position: Int) {
        if (isChanged(holderInfo.data)) {
            (holderInfo.data as? DATA)?.run {
                onDataChanged(holderInfo.data, position)
            }
        } else {
            (holderInfo.data as? DATA)?.run {
                onDataDuplicated(holderInfo.data, position)
            }
        }
    }

    private fun isChanged(data: Any?): Boolean {
        if (data == null) {
            return false
        }

        if (itemView.tag === data) {
            return false
        }
        itemView.tag = data
        return true
    }
}