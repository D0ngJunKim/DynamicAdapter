package com.dynamicadapter.example.ui.home.unit.full

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.dynamicadapter.example.databinding.ItemHomeFullBinding
import com.dynamicadapter.lib.helper.sticky.StickyLayout
import com.dynamicadapter.lib.holder.GlobalBindingViewHolder

/**
 * Created by Dong Jun Kim on 2024/06/11.
 */
class FullHolder(parent: ViewGroup, property: CommonProperty) :
        GlobalBindingViewHolder<ItemHomeFullBinding, FullUiData>(
                ItemHomeFullBinding.inflate(LayoutInflater.from(parent.context), parent, false), property) {

    override fun onDataChanged(data: FullUiData, position: Int) {
        vBinding.ivImage.setImageResource(data.imgResId)
        vBinding.tvPosition.text = "#$position"
        vBinding.tvDesc.setText(data.strResId)
        setOrientation(data)
    }

    override fun onDataDuplicated(data: FullUiData, position: Int) {
        setOrientation(data)
    }

    private fun setOrientation(data: FullUiData) {
        if (data.isVertical) {
            vBinding.gpTexts.isVisible = true
            setLayoutWidth(vBinding.root, ViewGroup.LayoutParams.MATCH_PARENT)
            setLayoutWidth(vBinding.lyFull, ViewGroup.LayoutParams.MATCH_PARENT)
            setLayoutHeight(vBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT)
            setLayoutHeight(vBinding.lyFull, ViewGroup.LayoutParams.WRAP_CONTENT)
        } else {
            vBinding.gpTexts.isVisible = false
            setLayoutWidth(vBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT)
            setLayoutWidth(vBinding.lyFull, ViewGroup.LayoutParams.WRAP_CONTENT)
            setLayoutHeight(vBinding.root, ViewGroup.LayoutParams.MATCH_PARENT)
            setLayoutHeight(vBinding.lyFull, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun setLayoutWidth(view: View, width: Int) {
        if (view.layoutParams.width != width) {
            view.updateLayoutParams {
                this.width = width
            }
        }
    }

    private fun setLayoutHeight(view: View, height: Int) {
        if (view.layoutParams.height != height) {
            view.updateLayoutParams {
                this.height = height
            }
        }
    }


    override fun getStickyLayout(): StickyLayout {
        return vBinding.root
    }
}