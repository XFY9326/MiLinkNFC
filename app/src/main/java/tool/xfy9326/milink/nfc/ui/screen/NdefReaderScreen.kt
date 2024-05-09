package tool.xfy9326.milink.nfc.ui.screen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import lib.xfy9326.xiaomi.nfc.XiaomiNdefType
import tool.xfy9326.milink.nfc.R
import tool.xfy9326.milink.nfc.data.nfc.NdefData
import tool.xfy9326.milink.nfc.data.nfc.NfcTag
import tool.xfy9326.milink.nfc.data.nfc.RawNdefData
import tool.xfy9326.milink.nfc.data.nfc.SmartPosterNdefData
import tool.xfy9326.milink.nfc.data.nfc.XiaomiNdefData
import tool.xfy9326.milink.nfc.ui.common.InfoContent
import tool.xfy9326.milink.nfc.ui.theme.AppTheme
import tool.xfy9326.milink.nfc.ui.theme.LocalAppThemeTypography
import tool.xfy9326.milink.nfc.ui.vm.NdefReaderViewModel
import tool.xfy9326.milink.nfc.utils.showToast

@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        NdefReaderScreen(
            onNavBack = {},
            onRequestImportNdefBin = {}
        )
    }
}

private const val ANIMATION_LABEL_CONTENT = "Content"

@Composable
fun NdefReaderScreen(
    viewModel: NdefReaderViewModel = viewModel(),
    onNavBack: () -> Unit,
    onRequestImportNdefBin: () -> Unit
) {
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
        Crossfade(targetState = uiState.value.hasData, label = ANIMATION_LABEL_CONTENT) {
            if (it) {
                NfcContent(innerPadding = innerPadding, uiState = uiState.value)
            } else {
                Box(
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
        title = { Text(text = stringResource(id = R.string.nfc_read_ndef)) },
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
private fun EventHandler(viewModel: NdefReaderViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.instantMsg.collectLatest {
            context.showToast(it.resId)
        }
    }
}

@Composable
private fun NfcContent(
    innerPadding: PaddingValues,
    uiState: NdefReaderViewModel.UiState,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .displayCutoutPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        uiState.nfcTag?.let { tag ->
            item {
                NfcTagInfoCard(modifier = Modifier.padding(horizontal = 8.dp), data = tag)
            }
        }
        itemsIndexed(uiState.ndefRecords) { index, item ->
            NdefDataCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                index = index,
                data = item
            )
        }
    }
}

@Composable
private fun NdefDataCard(
    modifier: Modifier = Modifier,
    index: Int,
    data: NdefData
) {
    when (data) {
        is RawNdefData -> RawNdefCard(
            modifier = modifier,
            index = index,
            data = data
        )

        is SmartPosterNdefData -> SmartPosterNdefCard(
            modifier = modifier,
            index = index,
            data = data
        )

        is XiaomiNdefData -> XiaomiNdefCard(
            modifier = modifier,
            index = index,
            data = data
        )

        else -> {}
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
private fun NfcTagInfoCard(modifier: Modifier = Modifier, data: NfcTag) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_nfc_tag),
            data = listOf(
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
private fun RawNdefCard(
    modifier: Modifier = Modifier,
    index: Int,
    data: RawNdefData
) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_ndef, index + 1),
            data = buildList {
                data.id?.let {
                    add(stringResource(id = R.string.ndef_field_id) to it)
                }
                add(stringResource(id = R.string.ndef_field_tnf) to data.tnf.name)
                data.type?.let {
                    add(stringResource(id = R.string.ndef_field_type) to it)
                }
                data.payloadLanguage?.let {
                    add(stringResource(id = R.string.ndef_field_language) to it)
                }
                data.payload?.let {
                    add(stringResource(id = R.string.ndef_field_payload) to it)
                }
            }
        )
    }
}

@Composable
private fun SmartPosterNdefCard(
    modifier: Modifier = Modifier,
    index: Int,
    data: SmartPosterNdefData
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                title = stringResource(id = R.string.info_sp_ndef, index + 1),
                data = listOf(
                    stringResource(id = R.string.ndef_sp_field_uri) to data.uri.toString()
                )
            )
            data.items.filterIsInstance<SmartPosterNdefData.MetaData>().takeIf {
                it.isNotEmpty()
            }?.let {
                SmartPosterMetaRecordCard(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    data = it
                )
            }
            data.items.filter {
                it !is SmartPosterNdefData.MetaData
            }.forEachIndexed { index, item ->
                NdefDataCard(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    index = index,
                    data = item
                )
            }
        }
    }
}

@Composable
private fun SmartPosterMetaRecordCard(
    modifier: Modifier = Modifier,
    data: List<SmartPosterNdefData.MetaData>
) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_sp_field_ndef),
            data = SmartPosterNdefData.sortFields(data).map {
                when (it) {
                    is SmartPosterNdefData.Title ->
                        stringResource(id = R.string.ndef_sp_field_title) to stringResource(
                            id = R.string.ndef_sp_field_language,
                            it.languageCode
                        ) + "\n" + it.text

                    is SmartPosterNdefData.Uri ->
                        stringResource(id = R.string.ndef_sp_field_uri) to it.uri.toString()

                    is SmartPosterNdefData.Action ->
                        stringResource(id = R.string.ndef_sp_field_action) to stringResource(id = it.actionType.resId)

                    is SmartPosterNdefData.Icon ->
                        stringResource(id = R.string.ndef_sp_field_icon) to it.payloadHex

                    is SmartPosterNdefData.Size ->
                        stringResource(id = R.string.ndef_sp_field_size) to
                                stringResource(id = R.string.ndef_sp_field_size_value, it.value)

                    is SmartPosterNdefData.Type ->
                        stringResource(id = R.string.ndef_sp_field_type) to it.mimeType
                }
            }
        )
    }
}

@Composable
private fun XiaomiNdefCard(
    modifier: Modifier = Modifier,
    index: Int,
    data: XiaomiNdefData
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            XiaomiNdefTypeCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                index = index,
                ndefType = data.ndefType
            )
            XiaomiNfcPayloadCard(
                modifier = Modifier.padding(horizontal = 8.dp),
                data = data.payload
            )
            when (data.appData) {
                is XiaomiNdefData.App.Handoff -> HandoffAppDataCard(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    data = data.appData
                )

                is XiaomiNdefData.App.NfcTag -> NfcTagAppDataCard(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    data = data.appData
                )
            }
        }
    }
}

@Composable
private fun XiaomiNdefTypeCard(
    modifier: Modifier = Modifier,
    index: Int,
    ndefType: XiaomiNdefType
) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_xiaomi_ndef, index + 1),
            data = listOf(
                stringResource(id = R.string.nfc_field_type) to stringResource(
                    id = when (ndefType) {
                        XiaomiNdefType.UNKNOWN -> R.string.unknown
                        XiaomiNdefType.SMART_HOME -> R.string.ndef_payload_type_smart_home
                        XiaomiNdefType.MI_CONNECT_SERVICE -> R.string.ndef_payload_type_mi_connect_service
                    }
                )
            )
        )
    }
}

@Composable
private fun XiaomiNfcPayloadCard(modifier: Modifier = Modifier, data: XiaomiNdefData.Payload) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_xiaomi_payload),
            data = buildList {
                add(stringResource(id = R.string.nfc_field_version) to "${data.majorVersion} ${data.minorVersion}")
                add(stringResource(id = R.string.nfc_field_protocol) to stringResource(id = data.protocol.resId))
                data.idHash?.let {
                    add(stringResource(id = R.string.nfc_field_id_hash) to it)
                }
            }
        )
    }
}

@Composable
private fun HandoffAppDataCard(modifier: Modifier = Modifier, data: XiaomiNdefData.App.Handoff) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_app_data),
            data = buildList {
                add(stringResource(id = R.string.nfc_field_version) to "${data.majorVersion} ${data.minorVersion}")
                add(stringResource(id = R.string.nfc_field_device_type) to data.deviceType)
                if (data.attributesMap.isNotEmpty()) {
                    add(
                        stringResource(id = R.string.nfc_field_attributes) to data.attributesMap.map { entry ->
                            "${entry.key}: ${entry.value}"
                        }.joinToString("\n")
                    )
                }
                add(stringResource(id = R.string.nfc_field_action) to data.action)
                if (data.payloadsMap.isNotEmpty()) {
                    add(
                        stringResource(id = R.string.nfc_field_properties) to data.payloadsMap.map { entry ->
                            "${entry.key}: ${entry.value}"
                        }.joinToString("\n")
                    )
                }
            }
        )
    }
}

@Composable
private fun NfcTagAppDataCard(modifier: Modifier = Modifier, data: XiaomiNdefData.App.NfcTag) {
    OutlinedCard(modifier = modifier) {
        InfoContent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = stringResource(id = R.string.info_app_data),
            data = listOf(
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
                    data = buildList {
                        add(stringResource(id = R.string.nfc_field_action) to record.action)
                        add(stringResource(id = R.string.nfc_field_condition) to record.condition)
                        add(stringResource(id = R.string.nfc_field_device_number) to record.deviceNumber)
                        add(stringResource(id = R.string.nfc_field_flags) to record.flags)
                        if (!record.conditionParameters.isNullOrEmpty()) {
                            add(stringResource(id = R.string.nfc_field_condition_parameters) to record.conditionParameters)
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
                    data = buildList {
                        add(stringResource(id = R.string.nfc_field_device_type) to record.deviceType)
                        add(stringResource(id = R.string.nfc_field_flags) to record.flags)
                        add(stringResource(id = R.string.nfc_field_device_number) to record.deviceNumber)
                        if (record.attributesMap.isNotEmpty()) {
                            add(
                                stringResource(id = R.string.nfc_field_attributes) to record.attributesMap.map { entry ->
                                    "${entry.key}: ${entry.value}"
                                }.joinToString("\n")
                            )
                        }
                    }
                )
            }
        }
    }
}