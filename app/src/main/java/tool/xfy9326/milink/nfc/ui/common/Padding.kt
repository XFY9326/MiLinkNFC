package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

@Composable
fun PaddingValues.update(
    horizontal: Dp? = null,
    vertical: Dp? = null
): PaddingValues {
    return update(
        start = horizontal,
        top = vertical,
        end = horizontal,
        bottom = vertical
    )
}

@Composable
fun PaddingValues.update(
    start: Dp? = null,
    top: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = start ?: calculateStartPadding(layoutDirection),
        top = top ?: calculateTopPadding(),
        end = end ?: calculateEndPadding(layoutDirection),
        bottom = bottom ?: calculateBottomPadding(),
    )
}

@Composable
fun PaddingValues.updateBy(
    horizontal: Dp? = null,
    vertical: Dp? = null
): PaddingValues {
    return updateBy(
        start = horizontal,
        top = vertical,
        end = horizontal,
        bottom = vertical
    )
}

@Composable
fun PaddingValues.updateBy(
    start: Dp? = null,
    top: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = start?.let { calculateStartPadding(layoutDirection) + it } ?: calculateStartPadding(layoutDirection),
        top = top?.let { calculateTopPadding() + it } ?: calculateTopPadding(),
        end = end?.let { calculateEndPadding(layoutDirection) + it } ?: calculateEndPadding(layoutDirection),
        bottom = bottom?.let { calculateBottomPadding() + it } ?: calculateBottomPadding(),
    )
}
