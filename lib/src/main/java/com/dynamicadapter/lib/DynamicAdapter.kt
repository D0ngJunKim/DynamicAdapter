package com.dynamicadapter.lib

import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dynamicadapter.lib.constants.GlobalViewType
import com.dynamicadapter.lib.constants.ILocalViewType
import com.dynamicadapter.lib.dao.HolderInfo
import com.dynamicadapter.lib.helper.sticky.GlobalStickyHelper
import com.dynamicadapter.lib.holder.ExceptionHolder
import com.dynamicadapter.lib.holder.GlobalViewHolder
import kotlin.enums.EnumEntries
import kotlin.math.max

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
abstract class DynamicAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), GlobalStickyHelper.IAdapter {
    // ViewType Map
    private val classMap: MutableMap<String, ClassInfo> = mutableMapOf()

    // Adapter Callback
    private val callbacks: ArrayList<AdapterCallback> = arrayListOf()

    var customProperty: GlobalViewHolder.CustomProperty? = null
    private var localViewTypes: EnumEntries<*>? = null

    val stickyHelper: GlobalStickyHelper = GlobalStickyHelper()


    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var holder: GlobalViewHolder? = null
        var clazzNm = ""
        var exceptionStr = ""

        try {
            holder = classMap.values.firstOrNull { classInfo -> classInfo.viewType == viewType }?.run {
                clazzNm = clazz.simpleName

                clazz.getConstructor(ViewGroup::class.java, GlobalViewHolder.CommonProperty::class.java)
                        .newInstance(parent, GlobalViewHolder.CommonProperty(customProperty))
            }
        } catch (e: Exception) {
            exceptionStr = e.stackTraceToString()
        }

        if (holder == null) {
            Toast.makeText(parent.context, "$clazzNm\n 생성 실패! \n $exceptionStr", Toast.LENGTH_SHORT).show()
            holder = ExceptionHolder(parent, GlobalViewHolder.CommonProperty(customProperty))
        }

        for (callback in callbacks) {
            callback.onViewHolderCreated(holder, viewType)
        }

        return holder
    }

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GlobalViewHolder) {
            for (callback in callbacks) {
                callback.onViewHolderPreBind(holder, position)
            }

            getCurrentList().getOrNull(position)?.run {
                holder.setIndexInfo(indexInfo)
                holder.setData(this, position)
                isDataChanged = false
            }

            for (callback in callbacks) {
                callback.onViewHolderBound(holder, position)
            }
        }
    }

    abstract fun getCurrentList(): List<HolderInfo>


    override fun getItemCount(): Int {
        return getCurrentList().size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return getCurrentList().getOrNull(position)?.viewType ?: -1
    }

    fun <E> setLocalViewTypes(entries: EnumEntries<E>) where E : Enum<E>, E : ILocalViewType<E> {
        localViewTypes = entries
    }

    fun getSpanItem(position: Int): Int {
        return getCurrentList().getOrNull(position)?.spanCount
                ?: GridLayoutManager.DEFAULT_SPAN_COUNT
    }

    @CallSuper
    open fun submitList(dataList: List<HolderInfo>, runnable: Runnable = Runnable { }) {
        dataList.forEach {
            it.viewType = getDynamicViewType(it.clazz)
        }
    }

    private fun getDynamicViewType(clazz: Class<out GlobalViewHolder>?): Int {
        if (clazz != null) {
            var classInfo = classMap[clazz.name]

            if (classInfo == null) {
                // Step 1. 전역 ViewType 체크
                classInfo = GlobalViewType.entries.find { it.clazz.name == clazz.name }?.run {
                    ClassInfo(ordinal, clazz)
                }

                // Step 2. 지역 내 지정 ViewType 체크.
                if (classInfo == null) {
                    classInfo = localViewTypes?.find { (it as? ILocalViewType<*>)?.clazz?.name == clazz.name }?.run {
                        ClassInfo(GlobalViewType.entries.size + ordinal, clazz)
                    }
                }

                // Step 3. 데이터 기반 자동 ViewType 지정.
                if (classInfo == null) {
                    val entrySize = GlobalViewType.entries.size + (localViewTypes?.size ?: 0)

                    classInfo = ClassInfo((classMap.values.sortedWith { o1, o2 ->
                        o1?.viewType?.minus(o2?.viewType ?: 0) ?: 0
                    }.lastOrNull()?.run {
                        max(viewType, entrySize)
                    } ?: run {
                        // 초기 발번
                        entrySize
                    }) + 1, clazz)
                }

                classMap[clazz.name] = classInfo
            }

            return classInfo.viewType
        }

        return -1
    }

    final override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            val spanCount = getSpanItem(holder.adapterPosition)
            lp.isFullSpan = spanCount == GridLayoutManager.DEFAULT_SPAN_COUNT
        }

        if (holder is GlobalViewHolder) {
            holder.onAttachToWindow()
        }
    }

    final override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is GlobalViewHolder) {
            holder.onDetachedFromWindow()
        }
    }

    final override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is GlobalViewHolder) {
            holder.onViewRecycled()
        }
    }

    fun addViewHolderCallback(callback: AdapterCallback): Boolean {
        return callbacks.add(callback)
    }

    fun removeViewHolderCallback(callback: AdapterCallback): Boolean {
        return callbacks.remove(callback)
    }

    fun removeAllViewHolderCallbacks() {
        callbacks.clear()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        stickyHelper.attachToRecyclerView(recyclerView, this)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        classMap.clear()
        stickyHelper.detachFromRecyclerView(recyclerView)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    final override fun isStickableHolder(position: Int): Boolean {
        return getCurrentList().getOrNull(position)?.isSticky == true
    }

    abstract class AdapterCallback {
        open fun onViewHolderCreated(holder: GlobalViewHolder, viewType: Int) {

        }

        open fun onViewHolderPreBind(holder: GlobalViewHolder, position: Int) {

        }

        open fun onViewHolderBound(holder: GlobalViewHolder, position: Int) {

        }
    }

    private data class ClassInfo(val viewType: Int, val clazz: Class<out GlobalViewHolder>)
}