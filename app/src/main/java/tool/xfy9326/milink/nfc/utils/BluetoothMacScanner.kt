package tool.xfy9326.milink.nfc.utils

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.IntentCompat
import androidx.core.content.getSystemService
import tool.xfy9326.milink.nfc.AppContext
import java.lang.ref.WeakReference

class BluetoothMacScanner(activity: ComponentActivity) {
    companion object {
        val isSupported: Boolean
            get() = (AppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) ||
                    AppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) &&
                    AppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_COMPANION_DEVICE_SETUP)
    }

    private val weakActivity = WeakReference(activity)
    private val nonEmptyStringPattern = "^(?!\\s*\$).+".toPattern()
    private val deviceManager by lazy {
        AppContext.getSystemService<CompanionDeviceManager>()
    }

    private var scannerCallback: WeakReference<(String?) -> Unit>? = null

    private val intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>? =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION")
            weakActivity.get()
                ?.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                    try {
                        if (it.resultCode == Activity.RESULT_OK) {
                            val device = it.data?.let { data ->
                                IntentCompat.getParcelableExtra(
                                    data,
                                    CompanionDeviceManager.EXTRA_DEVICE,
                                    Parcelable::class.java
                                )
                            }
                            val macAddress = when (device) {
                                is BluetoothDevice -> device.address
                                is android.bluetooth.le.ScanResult -> device.device.address
                                else -> null
                            }
                            scannerCallback?.get()?.invoke(macAddress?.uppercase())
                            if (macAddress != null) {
                                deviceManager?.disassociate(macAddress)
                            }
                        }
                    } finally {
                        scannerCallback = null
                    }
                }
        } else null


    val isEnabled: Boolean
        get() = weakActivity.get()?.getSystemService<BluetoothManager>()?.adapter?.isEnabled
            ?: false

    private fun newParingRequest(): AssociationRequest =
        AssociationRequest.Builder().apply {
            if (AppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                addDeviceFilter(
                    BluetoothDeviceFilter.Builder()
                        .setNamePattern(nonEmptyStringPattern)
                        .build()
                )
            }
            if (AppContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                addDeviceFilter(
                    BluetoothLeDeviceFilter.Builder()
                        .setNamePattern(nonEmptyStringPattern)
                        .build()
                )
            }
        }.build()

    fun requestDevice(scannerCallback: (String?) -> Unit) {
        deviceManager?.associate(
            newParingRequest(),
            object : CompanionDeviceManager.Callback() {
                override fun onAssociationPending(intentSender: IntentSender) {
                    weakActivity.get()?.let {
                        ActivityCompat.startIntentSenderForResult(
                            it,
                            intentSender,
                            0,
                            null,
                            0,
                            0,
                            0,
                            null
                        )
                    }
                }

                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    scannerCallback(associationInfo.deviceMacAddress?.toString()?.uppercase())
                    deviceManager?.disassociate(associationInfo.id)
                }

                @Deprecated("Old API", ReplaceWith("onAssociationPending(chooserLauncher)"))
                override fun onDeviceFound(intentSender: IntentSender) {
                    this@BluetoothMacScanner.scannerCallback = WeakReference(scannerCallback)
                    intentSenderLauncher?.launch(IntentSenderRequest.Builder(intentSender).build())
                }

                override fun onFailure(error: CharSequence?) {}
            }, null
        )
    }
}