package com.dynamicadapter.example.ui.home.unit.span

import android.view.LayoutInflater
import android.view.ViewGroup
import com.dynamicadapter.example.databinding.ItemHomeSpanBinding
import com.dynamicadapter.lib.holder.GlobalBindingViewHolder

/**
 * Created by Dong Jun Kim on 2024/06/11.
 */
class SpanHolder(parent: ViewGroup, property: CommonProperty) :
        GlobalBindingViewHolder<ItemHomeSpanBinding, SpanUiData>(
                ItemHomeSpanBinding.inflate(LayoutInflater.from(parent.context), parent, false), property) {

    override fun onDataChanged(data: SpanUiData, position: Int) {
        vBinding.ivImage.setImageResource(data.imgResId)
        vBinding.tvPosition.text = "#$position"
        vBinding.tvDesc.setText(data.strResId)
    }
}