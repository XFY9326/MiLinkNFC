package tool.xfy9326.milink.nfc.ui.screen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import lib.xfy9326.xiaomi.nfc.XiaomiNdefTNF
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.ui.HandoffAppDataUI
import tool.xfy9326.milink.nfc.data.ui.NfcTagAppDataUI
import tool.xfy9326.milink.nfc.data.ui.NfcTagInfoUI
import tool.xfy9326.milink.nfc.data.ui.XiaomiNfcPayloadUI
import tool.xfy9326.milink.nfc.ui.common.InfoContent
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography
import tool.xfy9326.milink.nfc.ui.vm.XiaomiNfcReaderViewModel
import tool.xfy9326.milink.nfc.utils.showToast

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        XiaomiNfcReaderScreen(
            onNavBack = {},
            onRequestImportNdefBin = {}
        )
    }
}

private const val ANIMATION_LABEL_CONTENT = "Content"

@Composable
fun XiaomiNfcReaderScreen(
    viewModel: XiaomiNfcReaderViewModel = viewModel(),
    onNavBack: () -> Unit,
    onRequestImportNdefBin: () -> Unit
) {
    val scrollState = rememberScrollState()

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                canExportNdefBin = uiState.value.canExportNdefBin,
                onNavBack = onNavBack,
                onRequestExportNdefBin = viewModel::requestExportNdefBin,
                onRequestImportNdefBin = onRequestImportNdefBin
            )
        }
    ) { innerPadding ->
        Crossfade(targetState = uiState.value.nfcInfo, label = ANIMATION_LABEL_CONTENT) { nfcInfo ->
            nfcInfo?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .displayCutoutPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    it.tag?.let { tag ->
                        NfcTagInfoCard(modifier = Modifier.padding(horizontal = 8.dp), data = tag)
                    }
                    NdefCard(modifier = Modifier.padding(horizontal = 8.dp), ndefType = it.ndefType)
                    XiaomiNfcPayloadCard(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        data = it.payload
                    )
                    when (it.appData) {
                        is HandoffAppDataUI -> HandoffAppDataCard(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            data = it.appData
                        )

                        is NfcTagAppDataUI -> NfcTagAppDataCard(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            data = it.appData
                        )
                    }
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .displayCutoutPadding(),
                contentAlignment = Alignment.Center
            ) {
                NfcWaitScan()
            }
        }
    }
    EventHandler(
        viewModel = viewModel
    )
}

@Composable
private fun TopBar(
    canExportNdefBin: Boolean,
    onNavBack: () -> Unit,
    onRequestExportNdefBin: () -> Unit,
    onRequestImportNdefBin: () -> Unit
) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.nfc_read_xiaomi_ndef)) },
        navigationIcon = {
            IconButton(onClick = onNavBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.nav_back)
                )
            }
        },
        actions = {
            IconButton(onClick = onRequestImportNdefBin) {
                Icon(
                    imageVector = Icons.Outlined.FileOpen,
                    contentDescription = stringResource(id = R.string.import_text)
                )
            }
            AnimatedVisibility(visible = canExportNdefBin) {
                IconButton(onClick = onRequestExportNdefBin) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = stringResource(id = R.string.save)
                    )
                }
            }
        }
    )
}

@Composable
private fun EventHandler(viewModel: XiaomiNfcReaderViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.instantMsg.collectLatest {
            context.showToast(it.resId)
        }
    }
}

@Composable
private fun NfcWaitScan() {
    val typography = LocalAppThemeTypography.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(62.dp),
            imageVector = Icons.Default.Nfc,
            contentDescription = stringResource(id = R.string.tap_and_read_nfc_tag),
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = stringResource(id = R.string.tap_and_read_nfc_tag),
            textAlign = TextAlign.Center,
            style = typography.bodyLarge
        )
    }
}

@StringRes
private fun Boolean.stringResId(): Int =
    if (this) R.string.content_true else R.string.content_false

@Composable
private fun NfcTagInfoCard(modifier: Modifier = Modifier, data: NfcTagInfoUI) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_nfc_tag),
            data = mapOf(
                stringResource(id = R.string.nfc_field_tech) to data.techList.joinToString(),
                stringResource(id = R.string.nfc_field_type) to data.type,
                stringResource(id = R.string.nfc_field_size) to stringResource(
                    id = R.string.current_and_total_bytes,
                    data.currentSize,
                    data.maxSize
                ),
                stringResource(id = R.string.nfc_field_writeable) to stringResource(id = data.writeable.stringResId()),
                stringResource(id = R.string.nfc_field_can_make_read_only) to stringResource(id = data.canMakeReadOnly.stringResId())
            )
        )
    }
}

@Composable
private fun NdefCard(modifier: Modifier = Modifier, ndefType: XiaomiNdefTNF) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_ndef),
            data = mapOf(
                stringResource(id = R.string.nfc_field_type) to stringResource(
                    id = when (ndefType) {
                        XiaomiNdefTNF.UNKNOWN -> R.string.unknown
                        XiaomiNdefTNF.SMART_HOME -> R.string.ndef_payload_type_smart_home
                        XiaomiNdefTNF.MI_CONNECT_SERVICE -> R.string.ndef_payload_type_mi_connect_service
                    }
                )
            )
        )
    }
}

@Composable
private fun XiaomiNfcPayloadCard(modifier: Modifier = Modifier, data: XiaomiNfcPayloadUI) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_xiaomi_payload),
            data = mutableMapOf(
                stringResource(id = R.string.nfc_field_version) to "${data.majorVersion} ${data.minorVersion}",
                stringResource(id = R.string.nfc_field_protocol) to stringResource(id = data.protocol.resId)
            ).also {
                if (data.idHash != null) it[stringResource(id = R.string.nfc_field_id_hash)] =
                    data.idHash
            }
        )
    }
}

@Composable
private fun HandoffAppDataCard(modifier: Modifier = Modifier, data: HandoffAppDataUI) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_app_data),
            data = mutableMapOf(
                stringResource(id = R.string.nfc_field_version) to "${data.majorVersion} ${data.minorVersion}",
                stringResource(id = R.string.nfc_field_device_type) to data.deviceType,
            ).also {
                if (data.attributesMap.isNotEmpty()) {
                    it[stringResource(id = R.string.nfc_field_attributes)] =
                        data.attributesMap.map { entry ->
                            "${entry.key}: ${entry.value}"
                        }.joinToString("\n")
                }
                it[stringResource(id = R.string.nfc_field_action)] = data.action
                if (data.payloadsMap.isNotEmpty()) {
                    it[stringResource(id = R.string.nfc_field_properties)] =
                        data.payloadsMap.map { entry ->
                            "${entry.key}: ${entry.value}"
                        }.joinToString("\n")
                }
            }
        )
    }
}

@Composable
private fun NfcTagAppDataCard(modifier: Modifier = Modifier, data: NfcTagAppDataUI) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_app_data),
            data = mapOf(
                stringResource(id = R.string.nfc_field_version) to "${data.majorVersion} ${data.minorVersion}",
                stringResource(id = R.string.nfc_field_write_time) to data.writeTime,
                stringResource(id = R.string.nfc_field_flags) to data.flags,
            )
        )
        data.actionRecord?.let { record ->
            OutlinedCard(modifier = Modifier.padding(10.dp)) {
                InfoContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    title = stringResource(id = R.string.info_app_data_nfc_tag_action_record),
                    data = mutableMapOf(
                        stringResource(id = R.string.nfc_field_action) to record.action,
                        stringResource(id = R.string.nfc_field_condition) to record.condition,
                        stringResource(id = R.string.nfc_field_device_number) to record.deviceNumber,
                        stringResource(id = R.string.nfc_field_flags) to record.flags
                    ).also {
                        if (!record.conditionParameters.isNullOrEmpty()) {
                            it[stringResource(id = R.string.nfc_field_condition_parameters)] =
                                record.conditionParameters
                        }
                    }
                )
            }
        }
        data.deviceRecord?.let { record ->
            OutlinedCard(modifier = Modifier.padding(10.dp)) {
                InfoContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    title = stringResource(id = R.string.info_app_data_nfc_tag_device_record),
                    data = mutableMapOf(
                        stringResource(id = R.string.nfc_field_device_type) to record.deviceType,
                        stringResource(id = R.string.nfc_field_flags) to record.flags,
                        stringResource(id = R.string.nfc_field_device_number) to record.deviceNumber,
                    ).also {
                        if (record.attributesMap.isNotEmpty()) {
                            it[stringResource(id = R.string.nfc_field_attributes)] =
                                record.attributesMap.map { entry ->
                                    "${entry.key}: ${entry.value}"
                                }.joinToString("\n")
                        }
                    }
                )
            }
        }
    }
}