package com.dynamicadapter.lib.holder

import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import com.dynamicadapter.lib.dao.HolderInfo
import com.dynamicadapter.lib.helper.sticky.GlobalStickyHelper
import com.dynamicadapter.lib.helper.sticky.StickyLayout

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
abstract class GlobalViewHolder(itemView: View,
                                protected val property: CommonProperty) : RecyclerView.ViewHolder(itemView), GlobalStickyHelper.IViewHolder {
    private var _indexInfo: HolderInfo.IndexInfo? = null
    protected val indexInfo: HolderInfo.IndexInfo
        get() = requireNotNull(_indexInfo)
    private var _isAttachedToStickyHolder: Boolean = false
    protected val isAttachedToStickyHolder: Boolean
        get() = _isAttachedToStickyHolder

    fun setIndexInfo(indexInfo: HolderInfo.IndexInfo?) {
        this._indexInfo = indexInfo
    }

    open fun onAttachToWindow() {

    }

    open fun onDetachedFromWindow() {

    }

    open fun onViewRecycled() {

    }

    abstract fun setData(holderInfo: HolderInfo, position: Int)

    /**
     * Sticky 활성화의 필수 요소. null 리턴 시 Sticky 동작 하지 않는다.
     */
    override fun getStickyLayout(): StickyLayout? {
        return null
    }

    /**
     * Sticky 동작에 Px단위의 Offset을 주는 기능. StickyLayout의 Top + Offset 의 지점에서 Sticky가 동작한다.
     */
    @Px
    override fun getStickyOffset(): Int {
        return 0
    }

    /**
     * ViewHolder → StickyContainer 로 이동했을 때 호출.
     */
    @CallSuper
    override fun onAttachedToStickyContainer() {
        _isAttachedToStickyHolder = true
    }

    /**
     * StickyContainer → ViewHolder 로 이동했을 때 호출.
     */
    @CallSuper
    override fun onDetachedFromStickyContainer() {
        _isAttachedToStickyHolder = false
    }

    // 공통적으로 설정되는 Property
    class CommonProperty(val customProperty: CustomProperty? = null)

    // 각 화면 또는 영역마다 설정 가능한 Property
    abstract class CustomProperty
}