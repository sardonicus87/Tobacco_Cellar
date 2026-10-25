package com.sardonicus.tobaccocellar.ui.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity

@Composable
fun Modifier.exclusiveScroll(): Modifier {
    val nestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset, available: Offset, source: NestedScrollSource
            ): Offset { return (Offset(x = available.x, y = 0f)) } // return available

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return Velocity(x = available.x, y = 0f) }
        }
    }
    return this.nestedScroll(nestedScroll)
}