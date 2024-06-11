package com.dynamicadapter.lib.constants

import com.dynamicadapter.lib.holder.ExceptionHolder
import com.dynamicadapter.lib.holder.GlobalViewHolder

/**
 * Created by Dong Jun Kim on 2024/06/10.
 */
// RecyclerView는 기본적으로 ViewType 기반이므로 일부 Custom이 필요한 ViewType들을 지정하여 관리.
// 각 화면 영역 단위의 ViewType
interface ILocalViewType<E : Enum<E>> {
    val clazz: Class<out GlobalViewHolder>
}

enum class ABC(override val clazz: Class<out GlobalViewHolder>) : ILocalViewType<ABC> {
 S(ExceptionHolder::class.java) {
 }
}