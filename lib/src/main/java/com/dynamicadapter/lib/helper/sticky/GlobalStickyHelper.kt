package com.dynamicadapter.lib.helper.sticky

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.Px
import androidx.collection.ArrayMap
import androidx.core.view.isGone
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dynamicadapter.lib.R
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
class GlobalStickyHelper : RecyclerView.OnScrollListener() {
    private val stickyHolderList: ArrayMap<Int, RecyclerView.ViewHolder> = ArrayMap()
    private val rect = Rect()

    private var _stickyPosition = RecyclerView.NO_POSITION
    val stickyPosition: Int get() = _stickyPosition
    private var _stickyHolder: RecyclerView.ViewHolder? = null
    val stickyHolder: RecyclerView.ViewHolder? get() = _stickyHolder
    private var stickyContainer: ViewGroup? = null
    private var recycler: RecyclerView.Recycler? = null
    private var adapter: RecyclerView.Adapter<*>? = null

    fun reset() {
        stickyHolderList.clear()
        _stickyHolder = null
        _stickyPosition = RecyclerView.NO_POSITION
        stickyContainer?.removeAllViews()
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        updateOrClearStickyView(recyclerView)
    }

    fun attachToRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.removeOnScrollListener(this)
        clearStickyView()

        recyclerView.post {
            // ScrollListener 설정
            recyclerView.addOnScrollListener(this)
            // Container 생성
            recyclerView.parent.takeIf { it is ViewGroup }?.run {
                (this as ViewGroup).addView(
                        FrameLayout(this.context).also {
                            stickyContainer = it
                        }, this.indexOfChild(recyclerView) + 1,
                        ViewGroup.MarginLayoutParams(
                                ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                        )
                )
            }

            stickyContainer?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = recyclerView.marginTop
            }

            // ViewCache 설정
            recyclerView.setViewCacheExtension(object : RecyclerView.ViewCacheExtension() {
                override fun getViewForPositionAndType(recycler: RecyclerView.Recycler, position: Int, type: Int): View? {
                    this@GlobalStickyHelper.recycler = recycler
                    return if (position == _stickyHolder?.adapterPosition) {
                        _stickyHolder
                    } else {
                        stickyHolderList.values.firstOrNull { it.adapterPosition == position }
                    }?.run {
                        this.itemView
                    }
                }
            })
            this.adapter = adapter
            updateOrClearStickyView(recyclerView)
        }
    }

    fun detachFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnScrollListener(this)
        clearReferences()
    }

    private fun clearReferences() {
        removeViewFromParent(stickyContainer)
        recycler = null
        stickyContainer = null
        _stickyHolder = null
        stickyHolderList.clear()
        adapter = null
    }

    private fun updateOrClearStickyView(recyclerView: RecyclerView) {
        val position: Int = getStickyPosition(recyclerView)
        if (position > RecyclerView.NO_POSITION) {
            updateStickyView(recyclerView, position)
        } else {
            clearStickyView()
        }
    }

    private fun getStickyPosition(recyclerView: RecyclerView, positionEnd: Int = RecyclerView.NO_POSITION): Int {
        return getPrevStickyPosition(if (positionEnd == RecyclerView.NO_POSITION) {
            // Pull To Refresh 와 같이 OverScroll 케이스에 대한 방어 처리.
            findFirstVisibleItemPosition(recyclerView).takeIf { !(it == 0 && !isTranslated(recyclerView)) }?.run {
                this
            } ?: RecyclerView.NO_POSITION
        } else {
            positionEnd
        })
    }

    private fun findFirstVisibleItemPosition(recyclerView: RecyclerView): Int {
        return when (val lp = recyclerView.layoutManager) {
            is LinearLayoutManager -> {
                try {
                    lp.findFirstVisibleItemPosition()
                } catch (e: Exception) {
                    RecyclerView.NO_POSITION
                }
            }

            is StaggeredGridLayoutManager -> {
                try {
                    lp.findFirstVisibleItemPositions(null)[0]
                } catch (e: Exception) {
                    RecyclerView.NO_POSITION
                }
            }

            else -> {
                RecyclerView.NO_POSITION
            }
        }
    }

    private fun isTranslated(recyclerView: RecyclerView): Boolean {
        return recyclerView.findViewHolderForAdapterPosition(0)?.let {
            if (canScrollHorizontally(recyclerView)) {
                it.itemView.x < 0
            } else {
                it.itemView.y < 0
            }
        } ?: false
    }

    private fun getPrevStickyPosition(position: Int): Int {
        if (position == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        return adapter?.run {
            if (position >= this.itemCount) {
                RecyclerView.NO_POSITION
            } else {
                if (this is IAdapter) {
                    for (i in position downTo 0) {
                        if (isStickableHolder(i)) {
                            return i
                        }
                    }
                }
                RecyclerView.NO_POSITION
            }
        } ?: RecyclerView.NO_POSITION
    }

    private fun updateStickyView(recyclerView: RecyclerView, position: Int) {
        if (_stickyPosition != position) {
            val holder: RecyclerView.ViewHolder? = getStickyViewHolder(recyclerView, position)
            if (holder is IViewHolder) {
                holder.getStickyLayout()?.run {
                    if (holder.itemView.isAttachedToWindow) {
                        if (holder.itemView == this) {
                            if (holder.getStickyOffset() > 0) {
                                val anchor = if (canScrollHorizontally(recyclerView)) {
                                    holder.itemView.left
                                } else {
                                    holder.itemView.top
                                }
                                val size = if (canScrollHorizontally(recyclerView)) {
                                    holder.itemView.width
                                } else {
                                    holder.itemView.height
                                }

                                if (holder.getStickyOffset() >= size) {
                                    if (size == 0 && anchor == 0) {
                                        swapStickyView(holder, position)
                                    } else {
                                        rect.setEmpty()
                                        holder.itemView.getLocalVisibleRect(rect)
                                        val percent = if (canScrollHorizontally(recyclerView)) {
                                            rect.width().toFloat() / holder.itemView.measuredWidth * 100f
                                        } else {
                                            rect.height().toFloat() / holder.itemView.measuredHeight * 100f
                                        }
                                        // 전부 사라진 경우
                                        if (percent <= 0) {
                                            swapStickyView(holder, position)
                                        } else {
                                            if (canScrollHorizontally(recyclerView)) {
                                                if (rect.left == 0) {
                                                    rect.setEmpty()
                                                    holder.itemView.getHitRect(rect)
                                                    if (rect.left < 0 && rect.left < rect.right) {
                                                        swapStickyView(holder, position)
                                                    }
                                                }
                                            } else {
                                                if (rect.top == 0) {
                                                    rect.setEmpty()
                                                    holder.itemView.getHitRect(rect)
                                                    if (rect.top < 0 && rect.top < rect.bottom) {
                                                        swapStickyView(holder, position)
                                                    }
                                                }
                                            }

                                        }
                                    }
                                } else {
                                    if (anchor.absoluteValue >= holder.getStickyOffset()) {
                                        swapStickyView(holder, position)
                                    }
                                }
                            } else {
                                swapStickyView(holder, position)
                            }
                        } else {
                            if (holder.itemView is ViewGroup) {
                                val anchor = if (canScrollHorizontally(recyclerView)) {
                                    holder.itemView.left + this.left
                                } else {
                                    holder.itemView.top + this.top
                                }

                                if (anchor + holder.getStickyOffset() <= 0 ||
                                        holder.adapterPosition == RecyclerView.NO_POSITION) {
                                    swapStickyView(holder, position)
                                }
                            }
                        }
                    } else {
                        swapStickyView(holder, position)
                    }
                }
            }
        } else {
            val holder = recyclerView.findViewHolderForAdapterPosition(position)
            if (holder is IViewHolder) {
                if (holder.itemView != holder.getStickyLayout()) {
                    holder.getStickyLayout()?.takeIf { holder.itemView is ViewGroup }?.run {
                        val anchor = if (canScrollHorizontally(recyclerView)) {
                            holder.itemView.left + this.left
                        } else {
                            holder.itemView.top + this.top
                        }

                        if (anchor + holder.getStickyOffset() > 0) {
                            val newPosition: Int = getStickyPosition(recyclerView, position - 1)
                            if (!(position == 0 && newPosition == 0) && newPosition > RecyclerView.NO_POSITION) {
                                swapStickyView(getStickyViewHolder(recyclerView, newPosition), newPosition)
                            } else {
                                clearStickyView()
                            }
                        }
                    }
                } else {
                    val anchor = if (canScrollHorizontally(recyclerView)) {
                        holder.itemView.left.absoluteValue
                    } else {
                        holder.itemView.top.absoluteValue
                    }

                    if (anchor < holder.getStickyOffset()) {
                        val newPosition: Int = getStickyPosition(recyclerView, position - 1)
                        if (!(position == 0 && newPosition == 0) && newPosition > RecyclerView.NO_POSITION) {
                            swapStickyView(getStickyViewHolder(recyclerView, newPosition), newPosition)
                        } else {
                            clearStickyView()
                        }
                    }
                }
            }
        }

        translateStickyView(recyclerView)
    }

    private fun getStickyViewHolder(recyclerView: RecyclerView, position: Int): RecyclerView.ViewHolder? {
        var holder: RecyclerView.ViewHolder? = recyclerView.findViewHolderForAdapterPosition(position)
        if (holder == null) {
            recycler?.run {
                holder = recyclerView.getChildViewHolder(this.getViewForPosition(position))
            }
        }
        return holder
    }

    private fun clearStickyView() {
        _stickyHolder?.let { holder ->
            detachStickyView(holder)

            _stickyHolder = null
            _stickyPosition = RecyclerView.NO_POSITION
        }
        stickyHolderList.clear()
    }

    private fun swapStickyView(holder: RecyclerView.ViewHolder?, newPosition: Int) {
        _stickyPosition = newPosition
        //스티키 -> 홀더로 이동
        detachStickyView(_stickyHolder)

        _stickyHolder = holder
        if (holder != null && _stickyPosition != RecyclerView.NO_POSITION) {
            stickyHolderList[_stickyPosition] = holder
        }
        if (_stickyHolder?.isRecyclable == true) {
            _stickyHolder?.setIsRecyclable(false)
        }

        attachStickyView()

        (_stickyHolder as? IViewHolder)?.onAttachedToStickyContainer()
    }

    private fun translateStickyView(recyclerView: RecyclerView) {
        var headerOffset = 0
        for (i in 0 until recyclerView.childCount) {
            val nextChild: View? = recyclerView.getChildAt(i)
            if (nextChild != null) {
                val adapterPos: Int = recyclerView.getChildAdapterPosition(nextChild)
                val nextPosition = getStickyPosition(recyclerView, adapterPos)

                if (nextPosition != RecyclerView.NO_POSITION && _stickyPosition != nextPosition) {
                    val nextHolder = recyclerView.findContainingViewHolder(nextChild)

                    if (nextHolder is IViewHolder) {
                        val stickyLayout = nextHolder.getStickyLayout()

                        if (stickyLayout != null) {
                            if (nextHolder.itemView == stickyLayout) {
                                if (canScrollHorizontally(recyclerView)) {
                                    if (nextChild.left + nextHolder.getStickyOffset() > 0) {
                                        headerOffset = min(nextChild.left -
                                                (stickyContainer?.getChildAt(0)?.width ?: 0) +
                                                nextHolder.getStickyOffset() -
                                                (recyclerView.layoutManager?.getLeftDecorationWidth(nextChild)
                                                        ?: 0) -
                                                (recyclerView.layoutManager?.getRightDecorationWidth(nextChild)
                                                        ?: 0), 0)

                                        if (headerOffset < 0) {
                                            break
                                        }
                                    }
                                } else {
                                    if (nextChild.top + nextHolder.getStickyOffset() > 0) {
                                        headerOffset = min(nextChild.top -
                                                (stickyContainer?.getChildAt(0)?.height ?: 0) +
                                                nextHolder.getStickyOffset() -
                                                (recyclerView.layoutManager?.getTopDecorationHeight(nextChild)
                                                        ?: 0) -
                                                (recyclerView.layoutManager?.getBottomDecorationHeight(nextChild)
                                                        ?: 0), 0)

                                        if (headerOffset < 0) {
                                            break
                                        }
                                    }
                                }
                            } else {
                                if (nextChild is ViewGroup && nextHolder.getStickyLayout()?.parent == nextChild) {
                                    if (canScrollHorizontally(recyclerView)) {
                                        if (nextChild.left +
                                                (nextHolder.getStickyLayout()?.left ?: 0) +
                                                nextHolder.getStickyOffset() > 0) {
                                            headerOffset = min(
                                                    nextChild.left -
                                                            (stickyContainer?.getChildAt(0)?.width
                                                                    ?: 0) +
                                                            (nextHolder.getStickyLayout()?.left
                                                                    ?: 0) +
                                                            nextHolder.getStickyOffset() -
                                                            (recyclerView.layoutManager?.getLeftDecorationWidth(nextChild)
                                                                    ?: 0) -
                                                            (recyclerView.layoutManager?.getRightDecorationWidth(nextChild)
                                                                    ?: 0), 0)

                                            if (headerOffset < 0) {
                                                break
                                            }
                                        }
                                    } else {
                                        if (nextChild.top +
                                                (nextHolder.getStickyLayout()?.top
                                                        ?: 0) + nextHolder.getStickyOffset() > 0) {
                                            headerOffset = min(nextChild.top -
                                                    (stickyContainer?.getChildAt(0)?.height ?: 0) +
                                                    (nextHolder.getStickyLayout()?.top ?: 0) +
                                                    nextHolder.getStickyOffset() -
                                                    (recyclerView.layoutManager?.getTopDecorationHeight(nextChild)
                                                            ?: 0) -
                                                    (recyclerView.layoutManager?.getBottomDecorationHeight(nextChild)
                                                            ?: 0), 0)

                                            if (headerOffset < 0) {
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (canScrollHorizontally(recyclerView)) {
            stickyContainer?.translationX = headerOffset.toFloat()
        } else {
            stickyContainer?.translationY = headerOffset.toFloat()
        }
    }

    private fun canScrollHorizontally(recyclerView: RecyclerView): Boolean {
        return recyclerView.layoutManager?.canScrollHorizontally() == true
    }

    /**
     * 스티키 헤더 사이즈 조정 후 스티키 레이아웃에 추가
     */
    private fun attachStickyView() {
        _stickyHolder?.run {
            val stickyLayout: StickyLayout? = (this as? IViewHolder)?.getStickyLayout()

            if (stickyLayout != null) {
                val child = stickyLayout.getChildAt(0)
                if (child != null) {
                    if (!child.isGone) {
                        val lp = child.layoutParams
                        stickyLayout.setTag(R.id.stickyLayoutSize, lp.width to lp.height)
                        stickyLayout.updateLayoutParams {
                            width = child.measuredWidth
                            height = (child.measuredHeight + stickyLayout.paddingTop + stickyLayout.paddingBottom).takeIf { it > 0 }?.run {
                                this
                            } ?: ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    }

                    removeViewFromParent(child)
                    addViewToParent(stickyContainer, child)
                    child.requestLayout()
                }
            }
        }
    }

    private fun detachStickyView(holder: RecyclerView.ViewHolder?) {
        if (holder != null) {
            val child = stickyContainer?.getChildAt(0)
            val iVH = (holder as? IViewHolder)
            if (child != null) {
                stickyContainer?.removeAllViews()
                addViewToParent(iVH?.getStickyLayout(), child)
                if (!holder.isRecyclable) {
                    holder.setIsRecyclable(true)
                }
                iVH?.getStickyLayout()?.run {
                    val lpSize = getTag(R.id.stickyLayoutSize) as? Pair<*, *>
                    val lpWidth = lpSize?.first as? Int
                    val lpHeight = lpSize?.second as? Int

                    if (lpWidth != null && lpHeight != null) {
                        updateLayoutParams {
                            width = lpWidth
                            height = lpHeight
                        }
                    }
                }
                iVH?.onDetachedFromStickyContainer()
            }
        }
    }

    private fun removeViewFromParent(child: View?) {
        if (child != null) {
            val parent = child.parent
            if (parent is ViewGroup) {
                parent.removeView(child)
            }
        }
    }

    private fun addViewToParent(parent: ViewGroup?, child: View?) {
        try {
            if (child != null) {
                parent?.addView(child, ViewGroup.LayoutParams(child.width, child.height))
            }
        } catch (ignore: Exception) {

        }
    }

    interface IAdapter {
        fun isStickableHolder(position: Int): Boolean
    }

    interface IViewHolder {
        /**
         * Sticky 활성화의 필수 요소. null 리턴 시 Sticky 동작 하지 않는다.
         */
        fun getStickyLayout(): StickyLayout?

        /**
         * Sticky 동작에 Px단위의 Offset을 주는 기능. StickyLayout의 Top + Offset 의 지점에서 Sticky가 동작한다.
         */
        @Px
        fun getStickyOffset(): Int

        /**
         * ViewHolder → StickyContainer 로 이동했을 때 호출.
         */
        fun onAttachedToStickyContainer()

        /**
         * StickyContainer → ViewHolder 로 이동했을 때 호출.
         */
        fun onDetachedFromStickyContainer()
    }
}