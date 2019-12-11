package com.example.m6app_v001b001

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

const val CmdHff = 0xff.toByte()
const val CmdHd55 = 0x55.toByte()
const val CmdDevHost = 0x80.toByte()
const val CmdDevSrc = 0x30.toByte()
const val CmdDevAg = 0x00.toByte()

enum class CmdId(val value: Byte) {
    SET_HFP_PAIR_REQ(0x02.toByte()),
    SET_HFP_PAIR_RSP(0x03.toByte()),
    SET_SRC_VOL_REQ(0x06.toByte()),
    SET_SRC_VOL_RSP(0x07.toByte()),
    SET_HFP_VOL_REQ(0x08.toByte()),
    SET_HFP_VOL_RSP(0x09.toByte()),
    SET_HFP_STA_REQ(0x0a.toByte()),
    SET_HFP_STA_RSP(0x0b.toByte()),
    SET_HFP_EXT_STA_REQ(0x0c.toByte()),
    SET_HFP_EXT_STA_RSP(0x0d.toByte()),
    SET_HFP_PSKEY_REQ(0x10.toByte()),
    SET_HFP_PSKEY_RSP(0x11.toByte()),
    SET_AG_PSKEY_REQ(0x12.toByte()),
    SET_AG_PSKEY_RSP(0x13.toByte()),
    SET_HFP_LOCAL_NAME_REQ(0x14.toByte()),
    SET_HFP_LOCAL_NAME_RSP(0x15.toByte()),
    SET_AG_LOCAL_NAME_REQ(0x16.toByte()),
    SET_AG_LOCAL_NAME_RSP(0x17.toByte()),
    SET_HFP_FEATURE_REQ(0x1c.toByte()),
    SET_HFP_FEATURE_RSP(0x1d.toByte()),
    SET_AG_FEATURE_REQ(0x1e.toByte()),
    SET_AG_FEATURE_RSP(0x1f.toByte()),
    SET_DFU_REQ(0x3e.toByte()),
    SETDFU_RSP(0x3f.toByte()),

    GET_HFP_PAIR_REQ(0x42.toByte()),
    GET_HFP_PAIR_RSP(0x43.toByte()),
    GET_SRC_VOL_REQ(0x44.toByte()),
    GET_SRC_VOL_RSP(0x45.toByte()),
    GET_AG_VOL_REQ(0x46.toByte()),
    GET_AG_VOL_RSP(0x47.toByte()),
    GET_HFP_VOL_REQ(0x48.toByte()),
    GET_HFP_VOL_RSP(0x49.toByte()),
    GET_HFP_STA_REQ(0x4a.toByte()),
    GET_HFP_STA_RSP(0x4b.toByte()),
    GET_HFP_EXT_STA_REQ(0x4c.toByte()),
    GET_HFP_EXT_STA_RSP(0x4d.toByte()),
    GET_SRC_DEV_NO_REQ(0x4e.toByte()),
    GET_SRC_DEV_NO_RSP(0x4f.toByte()),
    GET_HFP_PSKEY_REQ(0x50.toByte()),
    GET_HFP_PSKEY_RSP(0x51.toByte()),
    GET_AG_PSKEY_REQ(0x52.toByte()),
    GET_AG_PSKEY_RSP(0x53.toByte()),
    GET_HFP_LOCAL_NAME_REQ(0x54.toByte()),
    GET_HFP_LOCAL_NAME_RSP(0x55.toByte()),
    GET_AG_LOCAL_NAME_REQ(0x56.toByte()),
    GET_AG_LOCAL_NAME_RSP(0x57.toByte()),
    GET_HFP_VRESION_REQ(0x58.toByte()),
    GET_HFP_VRESION_RSP(0x59.toByte()),
    GET_AG_VRESION_REQ(0x5a.toByte()),
    GET_AG_VRESION_RSP(0x5b.toByte()),
    GET_HFP_FEATURE_REQ(0x5c.toByte()),
    GET_HFP_FEATURE_RSP(0x5d.toByte()),
    GET_AG_FEATURE_REQ(0x5e.toByte()),
    GET_AG_FEATURE_RSP(0x5f.toByte()),
    GET_HFP_RSSI_REQ(0x70.toByte()),
    GET_HFP_RSSI_RSP(0x71.toByte())
}

private const val KotlinLog = "kotlinTest"
private val iMageBtServiceRequest: Int = 3

data class FunctionFeature(
    var feature: Int = 0x00,
    var maxSlaveNo: Byte = 0x00,
    var maxTalkNo: Byte = 0x00,
    var led0: Int = 0x00,
    var led1: Int = 0x00,
    var led2: Int = 0x00,
    var led3: Int = 0x00)

data class VolumeStruct(
    var wiredMic: Byte = 0x00,
    var wiredSpkr: Byte = 0x00,
    var usbMic: Byte = 0x00,
    var usbSpkr: Byte = 0x00,
    var btMic: Byte = 0x00,
    var btSpkr: Byte = 0x00,
    var vcsMic: Byte = 0x00,
    var vcsSpkr: Byte = 0x00,
    var wiredAv: Byte = 0x00,
    var usbAv: Byte = 0x00,
    var btAv: Byte = 0x00,
    var vcsAv: Byte = 0x00,
    var decade: Byte = 0x00,
    var spkr: Byte = 0x00)

class BluetoothBase() {
    var btAddr: String = "C4:FF:BC:4F:FE:88"
    var btIdType: Int = 0x00
    var firmwareVersion: String = "iMage firmware"
    var localName: String = "iMage Device Name"
    var funFeature: FunctionFeature = FunctionFeature()
    var conStatus: Int = 0
    var extStatus: Int = 0
    var volumeLevel: VolumeStruct = VolumeStruct()
    var volumeMax: VolumeStruct = VolumeStruct()
    var deviceId: Int = 0
}

class BluetoothHfp() {
    var btBase = BluetoothBase()
    var deviceMode: Long = 0
    var rssi = 0
    var batLevel = 0
    var aliasName = "iMage Device Alias"
}

class BluetoothAg() {
    var btBase = BluetoothBase()
    var volumeOffset = VolumeStruct()
    var btHfp = Array(2) { BluetoothHfp() }
}

class BluetoothM6() {
    var btBase = BluetoothBase()
    var deviceMode: Long = 0
    var talkNo = 0
    var talkMode = 0
    var aliasName = "iMage Device Alias"
    var btAg =  Array<BluetoothAg>(3) { BluetoothAg() }
}

class MainActivity : AppCompatActivity() {
    var isiMageBtServiceStart = false
    var btM6Device = BluetoothM6()
    var btServiceData = iMageBtServiceData(0, 2)
    var intentImageBtService = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(KotlinLog, "Activity onCreate")
        // add broadcast filter
        var intentFilter = IntentFilter()
        intentFilter.addAction("iMageClientMessage")
        registerReceiver(iMageBtClientReceiver(), intentFilter)
        btInit()
        txv1.setOnClickListener {
            val getDevIdCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_SRC_DEV_NO_REQ.value , 1, 0x00, 0x00)
            val getSrcFwCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_PAIR_REQ.value , 0, 0x00)
            val getSrcVerCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_VRESION_REQ.value , 0, 0x00)
            val getSrcNameCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_LOCAL_NAME_REQ.value , 0, 0x00)
            val getSrcFeatureCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_FEATURE_REQ.value , 0, 0x00)
            val getSrcPskeyCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_PSKEY_REQ.value , 2, 0x00, 0x08, 0x00)
            val getSrcVolCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_SRC_VOL_REQ.value , 2, 0x00, 0x08, 0x00)
            val getAgFwCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_PAIR_REQ.value , 0, 0x00)
            val getAgVerCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_VRESION_REQ.value , 0, 0x00)
            val getAgNameCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_LOCAL_NAME_REQ.value , 0, 0x00)
            val getAgFeatureCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_FEATURE_REQ.value , 0, 0x00)
            val getAgPskeyCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_PSKEY_REQ.value , 2, 0x00, 0x08, 0x00)
            // val getAgPskeyCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.SET_HFP_PSKEY_REQ.value , 16, 0x00, 0x11, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x00, 0x00, 0x00)
            val getAgVolCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_VOL_REQ.value , 0, 0x00)
            val getSrcStateCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_STA_REQ.value, 0, 0x00)
            val getSrcExtStaCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_EXT_STA_REQ.value, 0, 0x00)
            val getAgStateCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_STA_REQ.value, 0, 0x00)
            val getAgExtStaCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_EXT_STA_REQ.value, 0, 0x00)
            val getAgRssiCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_RSSI_REQ.value , 0, 0x00)

            btCmdSend(getDevIdCmd, 0)
            btCmdSend(getSrcFwCmd, 0)
            btCmdSend(getSrcNameCmd, 0)
            btCmdSend(getSrcVerCmd, 0)
            btCmdSend(getSrcFeatureCmd, 0)
            btCmdSend(getSrcPskeyCmd, 0)
            btCmdSend(getSrcVolCmd, 0)
            btCmdSend(getAgFwCmd, 0)
            btCmdSend(getAgNameCmd, 0)
            btCmdSend(getAgVerCmd, 0)
            btCmdSend(getAgFeatureCmd, 0)
            btCmdSend(getAgPskeyCmd, 0)
            btCmdSend(getAgVolCmd, 0)
            btCmdSend(getSrcStateCmd, 0)
            btCmdSend(getSrcExtStaCmd, 0)
            btCmdSend(getAgStateCmd, 0)
            btCmdSend(getAgExtStaCmd, 0)
            btCmdSend(getAgRssiCmd, 0)

            btCmdSend(getDevIdCmd, 1)
            btCmdSend(getSrcFwCmd, 1)
            btCmdSend(getSrcNameCmd, 1)
            btCmdSend(getSrcVerCmd, 1)
            btCmdSend(getSrcFeatureCmd, 1)
            btCmdSend(getSrcPskeyCmd, 1)
            btCmdSend(getSrcVolCmd, 1)
            btCmdSend(getAgFwCmd, 1)
            btCmdSend(getAgNameCmd, 1)
            btCmdSend(getAgVerCmd, 1)
            btCmdSend(getAgFeatureCmd, 1)
            btCmdSend(getAgPskeyCmd, 1)
            btCmdSend(getAgVolCmd, 1)
            btCmdSend(getSrcStateCmd, 1)
            btCmdSend(getSrcExtStaCmd, 1)
            btCmdSend(getAgStateCmd, 1)
            btCmdSend(getAgExtStaCmd, 1)
            btCmdSend(getAgRssiCmd, 1)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(KotlinLog, "Activity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(KotlinLog, "Activity onDestroy")
        if(isiMageBtServiceStart == true) {
            stopService(Intent(this, iMageBtService::class.java))
            unbindService(iMageBtServiceConn)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(KotlinLog, "requestCode $requestCode resultCode $resultCode")
        when (requestCode) {
            iMageBtServiceRequest -> {
                when(resultCode) {
                    Activity.RESULT_OK -> {
                        Log.d(KotlinLog, "Bluetooth result ENABLE")
                        btInit()
                    }
                    else -> {
                        Log.d(KotlinLog, "Bluetooth result DISABLE")
                        finish()
                    }
                }
            }
        }
    }

    // bind service
    private lateinit var btM6ServiceMsg : Messenger
    private val clientMsgHandler = Messenger(Handler(Handler.Callback {
        Log.d(KotlinLog, "receive service message ${it.what} ${it.arg1} ${it.arg2}")
        isiMageBtServiceStart = true
        when(it.what) {

        }
    }))
    private val iMageBtServiceConn = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val msg = Message.obtain(null, 0, 0, 0, btM6Device)

            Log.d(KotlinLog, "onServiceConnected")
            isiMageBtServiceStart = true
            btM6ServiceMsg = Messenger(service)
            msg.replyTo = clientMsgHandler
            btM6ServiceMsgSend(btM6ServiceMsg, msg)
            btServiceData.btDeviceNo = 0
            btServiceData.btGroup = 0
            sendBroadcast(Intent("iMageBtService.iMageBtServiceReceiver").putExtra("btServiceData", btServiceData))
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(KotlinLog, "onServiceDisconnected")
        }
    }

    fun btM6ServiceMsgSend(messenger: Messenger, msg: Message) {
        try {
            messenger.send(msg)
        } catch (e: IOException) {
            Log.d(KotlinLog, "client send message exception")
            e.printStackTrace()
        }
    }

    fun btInit() {
        Log.d(KotlinLog, "btInit")
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            Log.d(KotlinLog, "Bluetooth request permission")
        } else if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
            Log.d(KotlinLog, "Bluetooth enable")
            if(isiMageBtServiceStart == false) {
                intentImageBtService = Intent(this, iMageBtService::class.java)
                // bind service
                btServiceData.btDeviceNo = 0
                btServiceData.btGroup = 1
                bindService(intentImageBtService.putExtra("btServiceData", btServiceData), iMageBtServiceConn, Context.BIND_AUTO_CREATE)
                // start service
                btServiceData.btDeviceNo = 2
                btServiceData.btGroup = 4
                startService(intentImageBtService.putExtra("btServiceData", btServiceData))
            }
        } else {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), iMageBtServiceRequest)
            Log.d(KotlinLog, "Bluetooth disable")
        }
    }

    fun btCmdSend(cmd: ByteArray, btDevice: Int)
    {
        btServiceData.btDeviceNo = btDevice
        btServiceData.btGroup = 0
        System.arraycopy(cmd, 0, btServiceData.btCmd, 0, cmd[5] + 7)
        Log.d(KotlinLog, "btCmdSend")

        sendBroadcast(Intent("iMageBtService.iMageBtServiceReceiver").putExtra("btServiceData", btServiceData))
    }
}
