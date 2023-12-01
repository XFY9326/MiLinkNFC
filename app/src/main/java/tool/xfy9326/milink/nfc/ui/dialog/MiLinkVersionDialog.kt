package tool.xfy9326.milink.nfc.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.PackageData
import tool.xfy9326.milink.nfc.ui.common.DialogContentSurface
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val context = LocalContext.current

    AppTheme {
        MiLinkVersionDialog(
            packageData = mapOf(
                "com.example.app1" to PackageData(
                    applicationName = "Example",
                    packageName = "com.example.app1",
                    versionCode = 1234,
                    versionName = "1.0.0",
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_logo_24)!!
                ),
                "com.example.app2" to null
            ),
            onDismissRequest = {}
        )
    }
}

@Composable
fun MiLinkVersionDialog(
    packageData: Map<String, PackageData?>,
    onDismissRequest: () -> Unit
) {
    if (packageData.isNotEmpty()) {
        val typography = LocalAppThemeTypography.current

        val scrollState = rememberScrollState()

        AlertDialog(onDismissRequest = onDismissRequest) {
            DialogContentSurface {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.local_app_versions), style = typography.titleLarge)
                    Text(text = stringResource(id = R.string.local_app_versions_desc), style = typography.bodyMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((name, data) in packageData) {
                            PackageDataCard(name, data)
                        }
                    }
                }
            }
        }
    } else {
        onDismissRequest()
    }
}

@Composable
private fun PackageDataCard(packageName: String, data: PackageData?) {
    val typography = LocalAppThemeTypography.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally)
        ) {
            if (data != null) {
                Image(
                    modifier = Modifier.size(38.dp),
                    painter = rememberDrawablePainter(drawable = data.icon),
                    contentDescription = data.applicationName,
                )
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = data.applicationName, style = typography.titleMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = data.packageName, style = typography.bodySmall)
                    Text(text = data.versionName ?: data.versionCode.toString(), style = typography.bodySmall)
                }
            } else {
                Text(
                    modifier = Modifier.padding(vertical = 4.dp),
                    text = stringResource(id = R.string.missing_package, packageName),
                    color = Color.Red,
                    style = typography.labelMedium
                )
            }
        }
    }
}