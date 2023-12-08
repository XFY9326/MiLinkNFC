package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.utils.EMPTY
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        MacAddressTextField(
            value = "",
            upperCase = true,
            onValueChange = {}
        )
    }
}

@Composable
fun MacAddressTextField(
    modifier: Modifier = Modifier,
    upperCase: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit,
) {
    var isError by remember { mutableStateOf(value.isInputError()) }

    OutlinedTextField(
        value = value.toMacUpperCase(upperCase),
        onValueChange = {
            val newValue = it.reformatMacAddressCharacter().toMacUpperCase(upperCase)
            isError = newValue.isInputError()
            onValueChange(newValue)
        },
        modifier = modifier,
        label = {
            Text(stringResource(id = R.string.enter_mac_address))
        },
        placeholder = {
            Text(stringResource(id = R.string.placeholder_mac_address), color = Color.Gray)
        },
        trailingIcon = if (value.isEmpty()) null else {
            {
                IconButton(onClick = {
                    isError = false
                    onValueChange(EMPTY)
                }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = stringResource(id = R.string.clear))
                }
            }
        },
        isError = isError,
        supportingText = {
            Text(text = if (isError) stringResource(id = R.string.invalid_mac_address) else EMPTY)
        },
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Ascii
        ),
        singleLine = true
    )
}

private val macAddressCharacterRanges = arrayOf(
    '0'.rangeTo('9'),
    'a'.rangeTo('f'),
    'A'.rangeTo('F'),
)
private val incompleteMacAddressRegex = "^([0-9A-Fa-f]{2}:){0,5}([0-9A-Fa-f]{0,2})\$".toRegex()

private fun String.validateIncompleteMacAddressRegex(): Boolean = incompleteMacAddressRegex.matches(this)

private fun Char.isValidMacAddressCharacter(): Boolean =
    this == ':' || macAddressCharacterRanges.any { this in it }

private fun String.toMacUpperCase(enabled: Boolean): String =
    if (enabled && isNotEmpty()) uppercase(Locale.US) else this

private fun String.reformatMacAddressCharacter(): String =
    this@reformatMacAddressCharacter.filter { it.isValidMacAddressCharacter() }

private fun String.isInputError(): Boolean =
    isNotBlank() && !validateIncompleteMacAddressRegex()
