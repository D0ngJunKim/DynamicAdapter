package com.dynamicadapter.example.ui.home.unit.full

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Created by 180842 on 2024/06/11.
 */
class FullUiData(@DrawableRes val imgResId: Int,
                 @StringRes val strResId: Int) {
    var isVertical: Boolean = true
}