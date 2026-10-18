package jp.linkserver.pyrite.internal

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Glyph/Scheduler settings geometry; not configurable by an application. */
internal object PyriteDefaults {
    val cardPadding = 22.dp
    val contentGap = 14.dp
    fun cardShape() = RoundedCornerShape(22.dp)
    fun connectedShape(first: Boolean, last: Boolean) = RoundedCornerShape(
        topStart = if (first) 22.dp else 6.dp, topEnd = if (first) 22.dp else 6.dp,
        bottomStart = if (last) 22.dp else 6.dp, bottomEnd = if (last) 22.dp else 6.dp
    )
}
