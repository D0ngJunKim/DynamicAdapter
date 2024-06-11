package com.dynamicadapter.example.ui.home.unit.span

import com.dynamicadapter.example.R
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Created by Dong Jun Kim on 2024/06/11.
 */

private val IMG_LIST = arrayOf(R.drawable.ic_menu_camera, R.drawable.ic_menu_gallery, R.drawable.ic_menu_slideshow)
private val STR_LIST = arrayOf(R.string.item_camera, R.string.item_gallery, R.string.item_slideshow)

fun getSpanUiData(): SpanUiData {
    val index = Random.nextInt(0..2)

    return SpanUiData(IMG_LIST[index], STR_LIST[index])
}