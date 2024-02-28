package tool.xfy9326.milink.nfc.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.BuildConfig
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.ui.common.DialogContentSurface
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography
import tool.xfy9326.milink.nfc.utils.SPACE

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        AboutDialog {}
    }
}

private const val TAG_GITHUB_URL = "URL"
private const val TAG_GITEE_URL = "GITEE_URL"

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    val typography = LocalAppThemeTypography.current
    val uriHandler = LocalUriHandler.current

    val openSourceText = buildAnnotatedString {
        append(stringResource(id = R.string.open_source))
        pushStringAnnotation(
            tag = TAG_GITHUB_URL,
            annotation = stringResource(id = R.string.code_url_github)
        )
        withStyle(
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(stringResource(id = R.string.code_platform_github))
        }
        append(SPACE)
        append(SPACE)
        pushStringAnnotation(
            tag = TAG_GITEE_URL,
            annotation = stringResource(id = R.string.code_url_gitee)
        )
        withStyle(
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(stringResource(id = R.string.code_platform_gitee))
        }
        pop()
    }

    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        DialogContentSurface {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_logo_24),
                    contentDescription = stringResource(id = R.string.app_name),
                    tint = colorResource(id = R.color.ic_launcher_foreground),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(10.dp)
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = typography.titleMedium
                    )
                    Text(
                        text = stringResource(
                            id = R.string.version,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE
                        ),
                        style = typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(
                            id = R.string.author,
                            stringResource(id = R.string.author_name)
                        ), style = typography.bodyMedium
                    )
                    ClickableText(
                        text = openSourceText,
                        style = typography.bodyMedium
                    ) { offset ->
                        openSourceText.getStringAnnotations(
                            tag = TAG_GITHUB_URL,
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            uriHandler.openUri(it.item)
                        }
                        openSourceText.getStringAnnotations(
                            tag = TAG_GITEE_URL,
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            uriHandler.openUri(it.item)
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.open_source_license),
                        style = typography.bodyMedium
                    )
                }
            }
        }
    }
}
