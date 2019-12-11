package com.example.m6app_v001b001

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import java.io.IOException
import java.lang.Byte.parseByte
import java.lang.Integer.parseInt
import java.lang.System.arraycopy
import java.util.*

private const val KotlinService = "kotlinServiceTest"
private val btAdapter = BluetoothAdapter.getDefaultAdapter()
private val maxRfcRecDataLen = 1024
private val maxServiceBtDevice = 6
data class iMageBtServiceData(var btDeviceNo: Int = 0, var btGroup: Int = 0) : Parcelable {
    var btCmd = ByteArray(256 + 7)

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
        btCmd = parcel.createByteArray()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(btDeviceNo)
        parcel.writeInt(btGroup)
        parcel.writeByteArray(btCmd)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<iMageBtServiceData> {
        override fun createFromParcel(parcel: Parcel): iMageBtServiceData {
            return iMageBtServiceData(parcel)
        }

        override fun newArray(size: Int): Array<iMageBtServiceData?> {
            return arrayOfNulls(size)
        }
    }
}

class iMageBtService : Service() {
    var isServiceStart = false
    lateinit var clientHandler: Messenger
    var serviceBtDevice = Array<ServiceBtDevice>(maxServiceBtDevice) { ServiceBtDevice("C4:FF:BC:4F:FE:88", 0)}

    inner class ServiceBtDevice(var btBda: String, var btDeviceNo: Int) {
        var rfcSocket = btAdapter.getRemoteDevice("C4:FF:BC:4F:FE:88").createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
        var isBtConnected = false
        var btReceiverData = iMageBtServiceData(0, 0)
        var thread = Thread()
        val btReadThread = Runnable {
            var isRfcRecHead = false
            var isRfcRecCmd = false
            var rfcRecDataLen = 0
            val rfcRecData = ByteArray(maxRfcRecDataLen)

            Log.d(KotlinService, "bluetooth receiver thread running")
            while(rfcSocket.isConnected) {
                if(isRfcRecCmd == false) {
                    try {
                        // Log.d(BtServiceLog, "bluetooth read data $btDevice")
                        rfcRecDataLen += rfcSocket.inputStream.read(rfcRecData, rfcRecDataLen, maxRfcRecDataLen - rfcRecDataLen)
                    } catch (e: IOException) {
                        Log.d(KotlinService, "bluetooth read fail")
                        break
                    }
                }
                // Log.d(ktLog, "rfc receive data length: ${rfcRecDataLen}")
                isRfcRecCmd = false
                var i = 0
                while(i < rfcRecDataLen) {
                    //for(i: Int in 0 until rfcRecDataLen) {
                    // Log.d(ktLog, "check command header $i ${rfcReceiverCmd[i]} ${rfcReceiverCmd[i + 1]}")
                    if((rfcRecData[i] == 0xff.toByte()) && (rfcRecData[i + 1] == 0x55.toByte())) {
                        isRfcRecHead = true
                        // Log.d(ktLog, "rfc header is detected")
                        if(rfcRecDataLen >= i + rfcRecData[i + 5] + 7) {
                            // val rfcCmd = ByteArray(rfcRecData[i + 5] + 7)
                            // Log.d(ktLog, "rfc command is detected ${i}")
                            System.arraycopy(rfcRecData, i, btReceiverData.btCmd, 0, rfcRecData[i + 5] + 7)
                            rfcRecDataLen -= i + rfcRecData[i + 5] + 7
                            System.arraycopy(rfcRecData, i + 7 + rfcRecData[i + 5], rfcRecData, 0, maxRfcRecDataLen - (i + 7 + rfcRecData[i + 5]))
                            if(BtCheckSum(btReceiverData.btCmd)) {
                                isRfcRecCmd = true
                                // Log.d(KotlinService, "Receiver iMage command")
                                btReceiverData.btGroup = 0
                                btReceiverData.btDeviceNo = btDeviceNo
                                sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                                isRfcRecHead = false
                                i = 0
                                // Handler().postDelayed({
                                // }, 100)
                                // Thread.sleep(50)
                                continue
                            }
                        }
                        break
                    }
                    else
                        i++
                }
                if(isRfcRecHead == false) {
                    rfcRecDataLen = 0
                }
            }
            Log.d(KotlinService, "bluetooth $btDeviceNo disconnect and read thread free")
            isBtConnected = false
        }

        fun isConnect() = isBtConnected

        fun connect() {
            Thread(Runnable {
                Log.d(KotlinService, "\tconnect remote bluetooth address: ${btBda}")
                rfcSocket = btAdapter.getRemoteDevice(btBda).createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                if(rfcSocket.isConnected == false) {
                    for(i in 0 until 3) {
                        try {
                            Log.d(KotlinService, "bluetooth connecting $btDeviceNo")
                            rfcSocket.connect()
                        } catch (e: IOException) {
                            Log.d(KotlinService, "bluetooth connect fail $btDeviceNo")
                        }
                        if(rfcSocket.isConnected) {
                            thread = Thread(btReadThread)
                            thread.start()
                            break
                        } else
                            SystemClock.sleep(5000)
                    }
                }

                if(rfcSocket.isConnected) {
                    Log.d(KotlinService, "bluetooth connected $btDeviceNo")
                    isBtConnected = true
                } else {
                    Log.d(KotlinService, "bluetooth disconnect $btDeviceNo")
                    isBtConnected = false
                }
            }).start()
        }

        fun rfcCmdSend(cmdBuf: ByteArray) {
            if(isBtConnected) {
                BtCheckSum(cmdBuf)
                rfcSocket.outputStream.write(cmdBuf)
            }
        }

        fun close() {
            Log.d(KotlinService, "bluetooth $btDeviceNo socket close")
            rfcSocket.close()
            isBtConnected = false
        }
    }

    fun BtCheckSum(cmdBuf: ByteArray): Boolean {
        var chksum = 0
        for(i in 2 until cmdBuf[5].toUByte().toInt() + 6)
            chksum += cmdBuf[i].toInt().and(0xff)
        chksum = chksum.inv().and(0xff)
        if(chksum == (cmdBuf[cmdBuf[5].toUByte().toInt() + 6]).toInt().and(0xff))
            return true
        else {
            cmdBuf[cmdBuf[5].toUByte().toInt() + 6] = chksum.toByte()
            return false
        }
    }

    inner class iMageBtServiceBinder : Binder() {
        inner class iMageBtServiceHandler : Handler(Handler.Callback {
            Log.d(KotlinService, "receive service message ${it.what} ${it.arg1} ${it.arg2}")
            clientHandler = it.replyTo
            val clientMsg = Message.obtain(null, 0, 0, 1)
            clientMsgSend(clientHandler, clientMsg)
            when(it.what) {

            }
        })

        fun getService() : IBinder {
            return Messenger(iMageBtServiceHandler()).binder
        }
    }

    fun clientMsgSend(messenger: Messenger, msg: Message) {
        try {
            messenger.send(msg)
        } catch (e: IOException) {
            Log.d(KotlinService, "client send message exception")
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        val btServiceData: iMageBtServiceData = intent.getParcelableExtra("btServiceData")
        Log.d(KotlinService, "iMageBtService onBind ${btServiceData.btDeviceNo} ${btServiceData.btGroup}")
        btDeviceInit()
        return iMageBtServiceBinder().getService()
    }

    // start service
    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter()
        // val btServiceData = iMageBtServiceData(0, 0)

        intentFilter.addAction("iMageBtService.iMageBtServiceReceiver")
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)
        registerReceiver(iMageBtServiceReceiver(), intentFilter)
        // send broadcast message to client
        // sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btServiceData))
        Log.d(KotlinService, "iMageBtService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val btServiceData: iMageBtServiceData = intent!!.getParcelableExtra("btServiceData")

        Log.d(KotlinService, "iMageBtService onStartCommand ${btServiceData.btDeviceNo} ${btServiceData.btGroup}")
        btDeviceInit()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(KotlinService, "iMageBtService onDestroy")
        isServiceStart = false
    }

    // broadcast receiver
    inner class iMageBtServiceReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            var btReceiverData = iMageBtServiceData(0, 0)

            btReceiverData.btCmd[0] = 0xff.toByte()
            btReceiverData.btCmd[1] = 0x55.toByte()
            btReceiverData.btCmd[2] = 0x00.toByte()
            btReceiverData.btCmd[3] = 0x00.toByte()
            when(intent!!.action) {
                "iMageBtService.iMageBtServiceReceiver" -> {
                    val btServiceData: iMageBtServiceData = intent.getParcelableExtra("btServiceData")

                    Log.d(KotlinService, "iMage bluetooth service receiver message ${btServiceData.btDeviceNo} ${btServiceData.btGroup}")
                    when(btServiceData.btGroup) {
                        0 -> {
                            serviceBtDevice[btServiceData.btDeviceNo].rfcCmdSend(btServiceData.btCmd)
                        }
                        else -> {
                            Log.d(KotlinService, "other order")
                        }
                    }
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val btDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    btReceiverData.btGroup = 1
                    when(btDevice.address) {
                        serviceBtDevice[0].btBda -> {
                            btReceiverData.btDeviceNo = 0
                        }
                        serviceBtDevice[1].btBda -> {
                            btReceiverData.btDeviceNo = 1
                        }
                        else -> {
                            btReceiverData.btDeviceNo = -1
                        }
                    }
                    btReceiverData.btCmd[4] = 0xf1.toByte()
                    btReceiverData.btCmd[5] = 0x01.toByte()
                    btReceiverData.btCmd[6] = 0x00.toByte()
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_ACL_CONNECTED ${btDevice.name} ${btDevice.address}")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val btDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    btReceiverData.btGroup = 1
                    when(btDevice.address) {
                        serviceBtDevice[0].btBda -> {
                            btReceiverData.btDeviceNo = 0
                        }
                        serviceBtDevice[1].btBda -> {
                            btReceiverData.btDeviceNo = 1
                        }
                        else -> {
                            btReceiverData.btDeviceNo = -1
                        }
                    }
                    btReceiverData.btCmd[4] = 0xf1.toByte()
                    btReceiverData.btCmd[5] = 0x01.toByte()
                    btReceiverData.btCmd[6] = 0x01.toByte()
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_ACL_DISCONNECTED ${btDevice.name} ${btDevice.address}")
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val btDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val btState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    val btPrevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                    btReceiverData.btGroup = 1
                    when(btDevice.address) {
                        serviceBtDevice[0].btBda -> {
                            btReceiverData.btDeviceNo = 0
                        }
                        serviceBtDevice[1].btBda -> {
                            btReceiverData.btDeviceNo = 1
                        }
                        else -> {
                            btReceiverData.btDeviceNo = -1
                        }
                    }
                    btReceiverData.btCmd[4] = 0xfc.toByte()
                    btReceiverData.btCmd[5] = 0x02.toByte()
                    btReceiverData.btCmd[6] = btState.toByte()
                    btReceiverData.btCmd[7] = btPrevState.toByte()
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_BOND_STATE_CHANGED ${btState.toUInt().toString(16)} ${btPrevState.toUInt().toString(16)}")
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val btDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val btClass = intent.getIntExtra(BluetoothDevice.EXTRA_CLASS, BluetoothDevice.ERROR)
                    val btRssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                    var str = btDevice.address.split(":")
                    var s : Int

                    btReceiverData.btGroup = 1
                    btReceiverData.btDeviceNo = 0
                    btReceiverData.btCmd[4] = 0xf9.toByte()
                    if(btDevice.name != null)
                        btReceiverData.btCmd[5] = (btDevice.name.length * 2 + 7).toByte()
                    else
                        btReceiverData.btCmd[5] = 7.toByte()
                    btReceiverData.btCmd[6] = btRssi.toByte()
                    for(i in 0 .. 5) {
                        s = parseInt(str[i], 16)
                        btReceiverData.btCmd[i + 7] = s.toByte()
                    }
                    if(btDevice.name != null) {
                        for (i in 0 until btDevice.name.length) {
                            s = btDevice.name[i].toInt()
                            btReceiverData.btCmd[i * 2 + 13] = s.shr(8).toByte()
                            btReceiverData.btCmd[i * 2 + 1 + 13] = s.and(0x00ff).toByte()
                        }
                    }
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_FOUND ${btDevice.name} ${btDevice.address} ${btClass.toUInt().toString(16)}")
                }
                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    val btDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    var str = btDevice.address.split(":")
                    var s : Int

                    btReceiverData.btGroup = 1
                    when(btDevice.address) {
                        serviceBtDevice[0].btBda -> {
                            btReceiverData.btDeviceNo = 0
                        }
                        serviceBtDevice[1].btBda -> {
                            btReceiverData.btDeviceNo = 1
                        }
                        else -> {
                            btReceiverData.btDeviceNo = -1
                        }
                    }
                    btReceiverData.btCmd[4] = 0xfa.toByte()
                    btReceiverData.btCmd[5] = (btDevice.name.length * 2 + 7).toByte()
                    btReceiverData.btCmd[6] = 0x00.toByte()
                    for(i in 0 .. 5) {
                        s = parseInt(str[i], 16)
                        btReceiverData.btCmd[i + 7] = s.toByte()
                    }
                    for(i in 0 until btDevice.name.length) {
                        s = btDevice.name[i].toInt()
                        btReceiverData.btCmd[i * 2 + 13] = s.shr(8).toByte()
                        btReceiverData.btCmd[i * 2 + 1 + 13] = s.and(0x00ff).toByte()
                    }
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_NAME_CHANGED")
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothDevice.ERROR)
                    val btPrevState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothDevice.ERROR)

                    btReceiverData.btGroup = 1
                    btReceiverData.btDeviceNo = 0
                    btReceiverData.btCmd[4] = 0xf0.toByte()
                    btReceiverData.btCmd[5] = 0x02.toByte()
                    btReceiverData.btCmd[6] = btState.toByte()
                    btReceiverData.btCmd[7] = btPrevState.toByte()
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_STATE_CHANGED $btState $btPrevState")
                }
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                    val btDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val btState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothDevice.ERROR)
                    val btPrevState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, BluetoothDevice.ERROR)

                    btReceiverData.btGroup = 1
                    when(btDevice.address) {
                        serviceBtDevice[0].btBda -> {
                            btReceiverData.btDeviceNo = 0
                        }
                        serviceBtDevice[1].btBda -> {
                            btReceiverData.btDeviceNo = 1
                        }
                        else -> {
                            btReceiverData.btDeviceNo = -1
                        }
                    }
                    btReceiverData.btCmd[4] = 0xf2.toByte()
                    btReceiverData.btCmd[5] = 0x02.toByte()
                    btReceiverData.btCmd[6] = btState.toByte()
                    btReceiverData.btCmd[7] = btPrevState.toByte()
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_CONNECTION_STATE_CHANGED $btState $btPrevState")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    btReceiverData.btGroup = 1
                    btReceiverData.btDeviceNo = 0
                    btReceiverData.btCmd[4] = 0xf8.toByte()
                    btReceiverData.btCmd[5] = 0x01.toByte()
                    btReceiverData.btCmd[6] = 0x01.toByte()
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_DISCOVERY_STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    btReceiverData.btGroup = 1
                    btReceiverData.btDeviceNo = 0
                    btReceiverData.btCmd[4] = 0xf8.toByte()
                    btReceiverData.btCmd[5] = 0x01.toByte()
                    btReceiverData.btCmd[6] = 0x00.toByte()
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_DISCOVERY_FINISHED")
                }
                BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED -> {
                    val btName = intent.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME)
                    var s : Int

                    btReceiverData.btGroup = 1
                    btReceiverData.btDeviceNo = 0
                    btReceiverData.btCmd[4] = 0xf9.toByte()
                    btReceiverData.btCmd[5] = (btName.length * 2 + 7).toByte()
                    btReceiverData.btCmd[6] = 0x00.toByte()
                    btReceiverData.btCmd[7] = 0x00.toByte()
                    btReceiverData.btCmd[8] = 0x00.toByte()
                    btReceiverData.btCmd[9] = 0x00.toByte()
                    btReceiverData.btCmd[10] = 0x00.toByte()
                    btReceiverData.btCmd[11] = 0x00.toByte()
                    btReceiverData.btCmd[12] = 0x00.toByte()
                    for(i in 0 until btName.length) {
                        s = btName[i].toInt()
                        btReceiverData.btCmd[i * 2 + 13] = s.shr(8).toByte()
                        btReceiverData.btCmd[i * 2 + 1 + 13] = s.and(0x00ff).toByte()
                    }
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))

                    Log.d(KotlinService, "ACTION_LOCAL_NAME_CHANGED $btName")
                }
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                    val btMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothDevice.ERROR)
                    val btPrevMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, BluetoothDevice.ERROR)

                    btReceiverData.btGroup = 1
                    btReceiverData.btDeviceNo = 0
                    btReceiverData.btCmd[4] = 0xfd.toByte()
                    btReceiverData.btCmd[5] = 0x02.toByte()
                    btReceiverData.btCmd[6] = btMode.toByte()
                    btReceiverData.btCmd[7] = btPrevMode.toByte()
                    BtCheckSum(btReceiverData.btCmd)
                    sendBroadcast(Intent("iMageClientMessage").putExtra("btServiceData", btReceiverData))
                    Log.d(KotlinService, "ACTION_SCAN_MODE_CHANGED $btMode $btPrevMode")
                }
                else -> {
                    Log.d(KotlinService, "broadcast receiver other message")
                }
            }
        }
    }

    fun btDeviceInit() {
        if(isServiceStart == false) {
            for(i in 0 until maxServiceBtDevice) {
                serviceBtDevice[i].btDeviceNo = i
                serviceBtDevice[i].btReceiverData.btDeviceNo = i
            }
            if (serviceBtDevice[0].isConnect() == false) {
                serviceBtDevice[0].connect()
            }
            if (serviceBtDevice[1].isConnect() == false) {
                serviceBtDevice[1].btBda = "C4:FF:BC:4F:FE:86"
                serviceBtDevice[1].connect()
            }
        }
        isServiceStart = true
    }
}
