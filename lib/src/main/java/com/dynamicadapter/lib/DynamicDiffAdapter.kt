package com.dynamicadapter.lib

import android.annotation.SuppressLint
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.dynamicadapter.lib.dao.HolderInfo

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
class DynamicDiffAdapter : DynamicAdapter() {
    var headerInfo: HolderInfo? = null
    var footerInfo: HolderInfo? = null

    // Async Differ
    private val differ: AsyncListDiffer<HolderInfo> = AsyncListDiffer(AdapterListUpdateCallback(this),
            AsyncDifferConfig.Builder(object : DiffUtil.ItemCallback<HolderInfo>() {
                override fun areItemsTheSame(oldItem: HolderInfo, newItem: HolderInfo): Boolean {
                    return oldItem === newItem
                }

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: HolderInfo, newItem: HolderInfo): Boolean {
                    return oldItem.clazz == newItem.clazz && oldItem.data?.equals(newItem.data) == true &&
                            !oldItem.isDataChanged && !newItem.isDataChanged
                }
            }).build())

    override fun getCurrentList(): List<HolderInfo> = differ.currentList

    override fun submitList(dataList: List<HolderInfo>, runnable: Runnable) {
        val finalList: List<HolderInfo> = if (dataList.isNotEmpty()) {
            dataList.toMutableList().apply {
                headerInfo?.run {
                    add(0, this)
                }
                footerInfo?.run {
                    add(this)
                }
            }
        } else {
            dataList
        }

        super.submitList(finalList, runnable)
        differ.submitList(finalList, runnable)
    }
}