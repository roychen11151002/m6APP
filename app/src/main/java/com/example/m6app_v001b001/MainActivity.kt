package com.example.m6app_v001b001

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.os.SystemClock.sleep
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

private val maxServiceBtDevice = 2

const val CmdHff = 0xff.toByte()
const val CmdHd55  = 0x55.toByte()
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
    GET_SRC_VOL_REQ(0x46.toByte()),
    GET_SRC_VOL_RSP(0x47.toByte()),
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
    GET_HFP_RSSI_RSP(0x71.toByte()),
    GET_HFP_BDA_REQ(0x78.toByte()),
    GET_HFP_BDA_RSP(0x79.toByte()),
    GET_AG_BDA_REQ(0x7a.toByte()),
    GET_AG_BDA_RSP(0x7b.toByte())
}

private const val KotlinLog = "kotlinMainTest"
private val iMageBtServiceRequest: Int = 3

data class FunctionFeature(
    var feature: UShort = 0x00.toUShort(),
    var filterBda: String = "C4:FF:BC:4F:FE:00",
    var maxSlaveNo: UByte = 0x00.toUByte(),
    var maxTalkNo: UByte = 0x00.toUByte(),
    var led: UShortArray = ushortArrayOf(0x00.toUShort(), 0x00.toUShort(), 0x00.toUShort(), 0x00.toUShort()))

data class VolumeStruct(
    var wiredMic: UByte = 0x00.toUByte(),
    var wiredSpkr: UByte = 0x00.toUByte(),
    var usbMic: UByte = 0x00.toUByte(),
    var usbSpkr: UByte = 0x00.toUByte(),
    var btMic: UByte = 0x00.toUByte(),
    var btSpkr: UByte = 0x00.toUByte(),
    var vcsMic: UByte = 0x00.toUByte(),
    var vcsSpkr: UByte = 0x00.toUByte(),
    var wiredAv: UByte = 0x00.toUByte(),
    var usbAv: UByte = 0x00.toUByte(),
    var btAv: UByte = 0x00.toUByte(),
    var vcsAv: UByte = 0x00.toUByte(),
    var decade: UByte = 0x00.toUByte(),
    var spkr: UByte = 0x00.toUByte())

class BluetoothBase() {
    var btBda: String = "00:00:00:00:00:00"
    var btIdType = 0x00.toUByte()
    var firmwareVersion: String = "iMage firmware"
    var localName: String = "iMage Device Name"
    var aliasName = "iMage Device Alias"
    var funFeature: FunctionFeature = FunctionFeature()
    var conStatus = 0.toUInt()
    var extStatus = 0.toUShort()
    var volumeLevel: VolumeStruct = VolumeStruct()
    var volumeMax: VolumeStruct = VolumeStruct()
    var deviceId: Int = 0
}

class BluetoothHfp() {
    var btBase = BluetoothBase()
    var btPairBda: String = "00:00:00:00:00:00"
    var deviceNo = 0.toUByte()
    var deviceMode = 0.toUShort()
    var volHfp: UByte = 0x00.toUByte()
    var rssi = 0.toShort()
    var batLevel = 0.toShort()
}

class BluetoothAg() {
    var btBase = BluetoothBase()
    var volSrc= ushortArrayOf(0x00.toUShort(), 0x00.toUShort())
    var volOffset = VolumeStruct()
    var btHfp = Array(2) { BluetoothHfp() }
}

class BluetoothM6() {
    var btSrcHfp = BluetoothHfp()
    var deviceMode = 0
    var talkMode = 0.toUByte()
    var btAg =  Array<BluetoothAg>(3) { BluetoothAg() }
}
var btM6Device = BluetoothM6()

class MainActivity : AppCompatActivity() {
    var isiMageBtServiceStart = false
    var btServiceData = iMageBtServiceData(0, 2)
    var intentImageBtService = Intent()
    lateinit var preferData: SharedPreferences
    lateinit var preferDataEdit: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(KotlinLog, "Activity onCreate")
        // add broadcast filter
        var intentFilter = IntentFilter()
        intentFilter.addAction("iMageClientMessage")
        registerReceiver(iMageBtClientReceiver(), intentFilter)
        preferData = getSharedPreferences("iMageBda", Context.MODE_PRIVATE)
        preferDataEdit = preferData.edit()
        btInit()
        txv1.setOnClickListener {
            val getDevIdCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_SRC_DEV_NO_REQ.value , 1, 0x00, 0x00)
            val getSrcPairCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_PAIR_REQ.value , 0, 0x00)
            val getSrcBdaCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_BDA_REQ.value , 0, 0x00)
            val getSrcVerCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_VRESION_REQ.value , 0, 0x00)
            val getSrcNameCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_LOCAL_NAME_REQ.value , 0, 0x00)
            val getSrcFeatureCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_FEATURE_REQ.value , 0, 0x00)
            val getSrcPskeyCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_PSKEY_REQ.value , 2, 0x00, 0x08, 0x00)
            val getSrcVolCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_VOL_REQ.value , 2, 0x00, 0x08, 0x00)
            val getSrcStateCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_STA_REQ.value, 0, 0x00)
            val getSrcExtStaCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, CmdDevSrc, CmdId.GET_HFP_EXT_STA_REQ.value, 0, 0x00)
            val getAgPairCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_PAIR_REQ.value , 0, 0x00)
            val getHfpBdaCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_BDA_REQ.value , 0, 0x00)
            val getAgBdaCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_BDA_REQ.value , 0, 0x00)
            val getAgVerCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_VRESION_REQ.value , 0, 0x00)
            val getAgNameCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_LOCAL_NAME_REQ.value , 0, 0x00)
            val getAgFeatureCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_FEATURE_REQ.value , 0, 0x00)
            val getAgPskeyCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_AG_PSKEY_REQ.value , 2, 0x00, 0x08, 0x00)
            // val getAgPskeyCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.SET_HFP_PSKEY_REQ.value , 16, 0x00, 0x11, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x00, 0x00, 0x00)
            val getAgVolCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_SRC_VOL_REQ.value , 0, 0x00)
            val getAgStateCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_STA_REQ.value, 0, 0x00)
            val getAgExtStaCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_EXT_STA_REQ.value, 0, 0x00)
            val getAgRssiCmd = byteArrayOf(CmdHff, CmdHd55, CmdDevHost, 0x38, CmdId.GET_HFP_RSSI_REQ.value , 0, 0x00)

            btCmdSend(getDevIdCmd, 0, 0)
            btCmdSend(getSrcPairCmd, 0, 0)
            btCmdSend(getSrcBdaCmd, 0, 0)
            btCmdSend(getSrcNameCmd, 0, 0)
            btCmdSend(getSrcVerCmd, 0, 0)
            btCmdSend(getSrcFeatureCmd, 0, 0)
            btCmdSend(getSrcPskeyCmd, 0, 0)
            btCmdSend(getSrcVolCmd, 0, 0)
            btCmdSend(getAgPairCmd, 0, 0)
            btCmdSend(getHfpBdaCmd, 0, 0)
            btCmdSend(getAgBdaCmd, 0, 0)
            btCmdSend(getAgNameCmd, 0, 0)
            btCmdSend(getAgVerCmd, 0, 0)
            btCmdSend(getAgFeatureCmd, 0, 0)
            btCmdSend(getAgPskeyCmd, 0, 0)
            btCmdSend(getAgVolCmd, 0, 0)
            btCmdSend(getSrcStateCmd, 0, 0)
            btCmdSend(getSrcExtStaCmd, 0, 0)
            btCmdSend(getAgStateCmd, 0, 0)
            btCmdSend(getAgExtStaCmd, 0, 0)
            btCmdSend(getAgRssiCmd, 0, 0)

            btCmdSend(getDevIdCmd, 1, 0)
            btCmdSend(getSrcPairCmd, 1, 0)
            btCmdSend(getSrcBdaCmd, 1, 0)
            btCmdSend(getSrcNameCmd, 1, 0)
            btCmdSend(getSrcVerCmd, 1, 0)
            btCmdSend(getSrcFeatureCmd, 1, 0)
            btCmdSend(getSrcPskeyCmd, 1, 0)
            btCmdSend(getSrcVolCmd, 1, 0)
            btCmdSend(getAgPairCmd, 1, 0)
            btCmdSend(getAgBdaCmd, 1, 0)
            btCmdSend(getAgNameCmd, 1, 0)
            btCmdSend(getAgVerCmd, 1, 0)
            btCmdSend(getAgFeatureCmd, 1, 0)
            btCmdSend(getAgPskeyCmd, 1, 0)
            btCmdSend(getAgVolCmd, 1, 0)
            btCmdSend(getSrcStateCmd, 1, 0)
            btCmdSend(getSrcExtStaCmd, 1, 0)
            btCmdSend(getAgStateCmd, 1, 0)
            btCmdSend(getAgExtStaCmd, 1, 0)
            btCmdSend(getAgRssiCmd, 1, 0)
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
                        // btInit()
                    }
                    else -> {
                        Log.d(KotlinLog, "Bluetooth result DISABLE")
                        // finish()
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

            var btBda =  arrayOf("C4:FF:BC:4F:FE:00", "C4:FF:BC:4F:FE:00", "C4:FF:BC:4F:FE:00", "C4:FF:BC:4F:FE:00", "C4:FF:BC:4F:FE:00", "C4:FF:BC:4F:FE:00")
            var str: List<String>
            var s: Int
            val ctrlBtBdaCmd = byteArrayOf(CmdHff, CmdHd55, 0x00, 0x00, 0xe0.toByte() , 7, 0x00, 0xc4.toByte(), 0xff.toByte(), 0xbc.toByte(), 0x4f.toByte(), 0xfe.toByte(), 0x88.toByte(), 0x00)
            var ctrlBtConCmd = byteArrayOf(CmdHff, CmdHd55, 0x00, 0x00, 0xe2.toByte() , 7, 0x01, 0xc4.toByte(), 0xff.toByte(), 0xbc.toByte(), 0x4f.toByte(), 0xfe.toByte(), 0x88.toByte(), 0x00)
            val ctrlBtDiscoveryCmd = byteArrayOf(CmdHff, CmdHd55, 0x00, 0x00, 0xe6.toByte() , 0, 0x01, 0x00)
            val ctrlBtPairedCmd = byteArrayOf(CmdHff, CmdHd55, 0x00, 0x00, 0xe8.toByte() , 0, 0x00)
/*
            for(i in 0 until  maxServiceBtDevice) {
                preferDataEdit.putString("btBda$i", btBda[i])
            }
            preferDataEdit.apply()
*/
            for(i in 0 until  maxServiceBtDevice) {
                str = preferData.getString("btBda${i.toString()}", "00:00:00:00:00:00").split(':')

                for(j in 0 .. 5) {
                    s = Integer.parseInt(str[j], 16)
                    ctrlBtConCmd[j + 7] = s.toByte()
                }
                btCmdSend(ctrlBtConCmd, i, 1)
            }
            // btCmdSend(ctrlBtBdaCmd, 0, 1)
            // btCmdSend(ctrlBtDiscoveryCmd, 0, 1)
            // btCmdSend(ctrlBtPairedCmd, 0, 1)
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
                btServiceData.btGroup = 0
                bindService(intentImageBtService.putExtra("btServiceData", btServiceData), iMageBtServiceConn, Context.BIND_AUTO_CREATE)
                // start service
                btServiceData.btDeviceNo = 0
                btServiceData.btGroup = 0
                startService(intentImageBtService.putExtra("btServiceData", btServiceData))
            }
         } else {
            Log.d(KotlinLog, "Bluetooth disable")
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), iMageBtServiceRequest)
        }
    }

    fun btCmdSend(cmd: ByteArray, btDevice: Int, btGroup: Int) {
        btServiceData.btDeviceNo = btDevice
        btServiceData.btGroup = btGroup
        System.arraycopy(cmd, 0, btServiceData.btCmd, 0, cmd[5] + 7)
        // Log.d(KotlinLog, "btCmdSend")
        sendBroadcast(Intent("iMageBtService.iMageBtServiceReceiver").putExtra("btServiceData", btServiceData))
    }
}
