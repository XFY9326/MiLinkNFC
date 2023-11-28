package tool.xfy9326.milink.nfc.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tool.xfy9326.milink.nfc.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
private fun Preview() {
    var selection by rememberSaveable {
        mutableStateOf("A")
    }
    AppTheme {
        Column(modifier = Modifier.size(150.dp, 300.dp)) {
            SelectorTextField(
                modifier = Modifier.fillMaxWidth(),
                label = "Label",
                keyTextMap = mapOf("A" to "1", "B" to "2"),
                selectKey = selection,
                onKeySelected = {
                    selection = it
                }
            )
        }
    }
}

@Composable
fun SelectorTextField(
    modifier: Modifier = Modifier,
    label: String,
    keyTextMap: Map<String, String>,
    selectKey: String,
    onKeySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = keyTextMap[selectKey] ?: error("Key $selectKey not in keyTextMap"),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for ((key, text) in keyTextMap) {
                DropdownMenuItem(
                    text = { Text(text = text) },
                    onClick = {
                        // showText = keyTextMap[key] ?: error("Key $key not in keyTextMap")
                        expanded = false
                        onKeySelected(key)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}