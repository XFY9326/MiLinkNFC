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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.coerceIn
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
            title = "MAC",
            upperCase = true,
            onValueChange = {}
        )
    }
}

@Composable
fun MacAddressTextField(
    modifier: Modifier = Modifier,
    title: String,
    upperCase: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit,
) {
    var isError by remember { mutableStateOf(value.isInputError()) }
    var selection by remember { mutableStateOf(TextRange(value.length)) }
    var composition by remember { mutableStateOf<TextRange?>(null) }

    val textValue = TextFieldValue(value.formatMacAddress(upperCase), selection, composition)

    OutlinedTextField(
        value = textValue,
        onValueChange = {
            if (it.text.length <= MAC_ADDRESS_LENGTH) {
                val newValue = it.formatMacAddress(textValue, upperCase)
                selection = newValue.selection
                composition = newValue.composition
                isError = newValue.text.isInputError()
                if (value != newValue.text) {
                    onValueChange(newValue.text)
                }
            }
        },
        modifier = modifier,
        label = {
            Text(text = title)
        },
        placeholder = {
            Text(text = stringResource(id = R.string.placeholder_mac_address), color = Color.Gray)
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
            keyboardType = KeyboardType.Ascii,
            capitalization = if (upperCase) KeyboardCapitalization.Characters else KeyboardCapitalization.None
        ),
        singleLine = true
    )
}

private const val MAC_ADDRESS_DIVIDER = ':'
private const val MAC_ADDRESS_WIN_DIVIDER = '-'
private const val MAC_ADDRESS_LENGTH = 2 * 6 + 5

private val hexCharacterRanges = arrayOf(
    '0'.rangeTo('9'),
    'a'.rangeTo('f'),
    'A'.rangeTo('F'),
)
private val incompleteMacAddressRegex = "^([0-9A-Fa-f]{2}:){0,5}([0-9A-Fa-f]{0,2})\$".toRegex()

private fun String.isInputError(): Boolean =
    isNotBlank() && !incompleteMacAddressRegex.matches(this)

private fun String.formatMacAddress(upperCase: Boolean): String =
    asSequence().map {
        if (it == MAC_ADDRESS_WIN_DIVIDER) MAC_ADDRESS_DIVIDER else it
    }.filter {
        it == MAC_ADDRESS_DIVIDER || hexCharacterRanges.any { r -> it in r }
    }.map {
        if (upperCase) it.uppercase(Locale.US) else it
    }.joinToString(separator = EMPTY)

private fun String.insert(index: Int, c: Char): String =
    when (index) {
        0 -> c + this
        length -> this + c
        else -> buildString(length + 1) {
            append(this@insert.subSequence(0, index))
            append(c)
            append(this@insert.subSequence(index, this@insert.length))
        }
    }

private fun TextRange.applyOffset(offset: Int): TextRange =
    if (offset != 0) {
        TextRange(start = start + offset, end = end + offset)
    } else {
        this
    }

private fun TextFieldValue.formatMacAddress(lastValue: TextFieldValue, upperCase: Boolean): TextFieldValue {
    val isAdd = text.length > lastValue.text.length
    var newText = text.formatMacAddress(upperCase)
    var newSelection = selection.applyOffset(newText.length - text.length).coerceIn(0, newText.length)
    if (isAdd) {
        val pos = newSelection.start
        if (pos == newSelection.end && pos in 2..<MAC_ADDRESS_LENGTH) {
            if (pos <= newText.lastIndex && newText[pos] == MAC_ADDRESS_DIVIDER) {
                newSelection = TextRange(pos + 1)
            } else if (
                newText.length < MAC_ADDRESS_LENGTH &&
                newText[pos - 1] != MAC_ADDRESS_DIVIDER &&
                newText[pos - 2] != MAC_ADDRESS_DIVIDER
            ) {
                newText = if (pos >= 3 && newText[pos - 3] != MAC_ADDRESS_DIVIDER) {
                    newText.insert(pos - 1, MAC_ADDRESS_DIVIDER)
                } else {
                    newText.insert(pos, MAC_ADDRESS_DIVIDER)
                }
                newSelection = TextRange(pos + 1)
            }
        }
    }
    return copy(text = newText, selection = newSelection)
}
