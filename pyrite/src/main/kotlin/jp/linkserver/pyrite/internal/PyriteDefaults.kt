package jp.linkserver.pyrite.internal

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import jp.linkserver.pyrite.theme.LocalPyriteStyle
import jp.linkserver.pyrite.theme.PyriteStyle

/** Geometry owned by Pyrite; applications provide content and state. */
internal object PyriteDefaults {
    val cardPadding = 24.dp
    val contentGap = 16.dp
    val mosaicGap = 3.dp
    val rowHorizontalPadding = 22.dp
    val rowVerticalPadding = 18.dp

    @Composable fun cardShape(): CornerBasedShape =
        if (LocalPyriteStyle.current == PyriteStyle.Material3Expressive)
            MaterialTheme.shapes.extraLarge else MaterialTheme.shapes.large

    @Composable fun connectedShape(first: Boolean, last: Boolean): CornerBasedShape {
        val outer = cardShape()
        val inner = MaterialTheme.shapes.extraSmall
        return inner.copy(
            topStart = if (first) outer.topStart else inner.topStart,
            topEnd = if (first) outer.topEnd else inner.topEnd,
            bottomStart = if (last) outer.bottomStart else inner.bottomStart,
            bottomEnd = if (last) outer.bottomEnd else inner.bottomEnd
        )
    }
}
