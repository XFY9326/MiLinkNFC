package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import tool.xfy9326.milink.nfc.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        DialogContentSurface {}
    }
}

@Composable
fun DialogContentSurface(
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    content: @Composable () -> Unit
) {
    Surface(
        shape = shape,
        color = containerColor,
        tonalElevation = tonalElevation,
        content = content
    )
}