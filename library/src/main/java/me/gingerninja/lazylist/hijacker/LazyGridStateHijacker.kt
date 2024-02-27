package me.gingerninja.lazylist.hijacker

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember

/**
 * If [enabled] is `true`, it will override the internals of [LazyGridState] so that the grid would
 * not follow the first visible item in the grid by scrolling to it when moved.
 */
class LazyGridStateHijacker(
    private val gridState: LazyGridState,
    enabled: Boolean = true
) {
    private val scrollPositionField = gridState.javaClass.getDeclaredField("scrollPosition").apply {
        isAccessible = true
    }

    private val scrollPositionObj = scrollPositionField.get(gridState)

    private val lastKeyRemover: () -> Unit =
        scrollPositionField.type.getDeclaredField("lastKnownFirstItemKey").run {
            isAccessible = true

            fun() { set(scrollPositionObj, null) }
        }

    private val indexField = scrollPositionField.type.getDeclaredField("index\$delegate").apply {
        isAccessible = true
    }

    /**
     * Controls whether the hijack implementation is used or the default one.
     *
     * *Note: this is not backed by [androidx.compose.runtime.State]. You should not listen to the
     * changes of this in compose.*
     */
    var enabled: Boolean = enabled
        set(value) {
            if (field == value) {
                return
            }

            field = value

            setProps(value)
        }

    init {
        setProps(enabled)
    }

    private fun setProps(enable: Boolean) {
        val oldValue = indexField.get(scrollPositionObj).run {
            if (this is IntStateHijacker) {
                intValueDirect
            } else {
                gridState.firstVisibleItemIndex
            }
        }

        val mutableIntState: MutableIntState = if (enable) {
            IntStateHijacker(
                state = mutableIntStateOf(oldValue),
                keyRemover = lastKeyRemover
            )
        } else {
            mutableIntStateOf(oldValue)
        }

        indexField.set(scrollPositionObj, mutableIntState)
    }
}

/**
 * Creates a [LazyGridStateHijacker] that is remembered across compositions.
 *
 * Changes to the provided [gridState] value will result in the state hijacker being recreated.
 *
 * Changes to the provided [enabled] value will **not** result in the state hijacker being recreated,
 * however the [LazyGridStateHijacker.enabled] will be updated.
 *
 * @param gridState the state that will be hijacked by [LazyGridStateHijacker]
 * @param enabled the value for [LazyGridStateHijacker.enabled]
 *
 * @return [LazyGridStateHijacker] instance
 */
@Composable
fun rememberLazyGridStateHijacker(
    gridState: LazyGridState,
    enabled: Boolean = true
): LazyGridStateHijacker {
    return remember(gridState) {
        LazyGridStateHijacker(gridState, enabled)
    }.apply {
        this.enabled = enabled
    }
}
