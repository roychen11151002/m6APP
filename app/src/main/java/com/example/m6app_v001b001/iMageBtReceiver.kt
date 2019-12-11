package com.example.m6app_v001b001

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val KotlinclientReceiver = "kotlinTest"

class iMageBtClientReceiver : BroadcastReceiver() {

    @ExperimentalUnsignedTypes
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val btServiceData: iMageBtServiceData = intent.getParcelableExtra("btServiceData")
        // Log.d(KotlinclientReceiver, "client receive message ${btServiceData.btDeviceNo} ${btServiceData.btGroup}")
        var str = "group ${btServiceData.btGroup} device ${btServiceData.btDeviceNo}    "
        for(i in 0 until btServiceData.btCmd[5].toUByte().toInt() + 7) {
            str += btServiceData.btCmd[i].toUByte().toString(16).toUpperCase() + " "
        }
        Log.d(KotlinclientReceiver,  "$str")
        when(btServiceData.btGroup) {
            0 -> {
                rfcCmdParse(btServiceData.btCmd, btServiceData.btDeviceNo)
            }
            1 -> {
                rfcCmdParse(btServiceData.btCmd, btServiceData.btDeviceNo)
            }
            else -> {
            }
        }
    }

    @ExperimentalUnsignedTypes
    fun rfcCmdParse(cmdBuf: ByteArray, btDevice: Int) {
        when(cmdBuf[4]) {
            CmdId.SET_SRC_VOL_RSP.value -> Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} AG volume set")
            CmdId.SET_HFP_VOL_RSP.value -> Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} HFP volume set")
            CmdId.GET_HFP_STA_RSP.value -> {
                var state = cmdBuf[6].toUInt().and(0xff.toUInt()).shl(24) + cmdBuf[7].toUInt().and(0xff.toUInt()).shl(16) + cmdBuf[8].toUInt().and(0xff.toUInt()).shl(8) + cmdBuf[9].toUInt().and(0xff.toUInt())

                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} HFP status get ${state.toString(16)} ")
            }
            CmdId.GET_HFP_EXT_STA_RSP.value -> {
                // var staExt = cmdBuf[6].toUInt().and(0xff.toUInt()).shl(8) + cmdBuf[7].toUInt().and(0xff.toUInt())

                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} HFP extra status get")
            }
            CmdId.GET_HFP_LOCAL_NAME_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} local name: ${String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())}")
            }
            CmdId.GET_AG_LOCAL_NAME_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} local name: ${String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())}")
            }
            CmdId.GET_HFP_VRESION_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} firmware version: ${String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())}")
            }
            CmdId.GET_AG_VRESION_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} firmware version: ${String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())}")
            }
            CmdId.GET_SRC_DEV_NO_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} source device number ${cmdBuf[6].toUByte().toString(16)}")
            }
            CmdId.GET_HFP_VOL_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} volume ${cmdBuf[6].toUByte().toString(16)}")
            }
            CmdId.GET_HFP_PAIR_RSP.value -> {
                var bdaLap = cmdBuf[6].toUInt().and(0xff.toUInt()).shl(24) + cmdBuf[7].toUInt().and(0xff.toUInt()).shl(16) + cmdBuf[8].toUInt().and(0xff.toUInt()).shl(8) + cmdBuf[9].toUInt().and(0xff.toUInt())
                var bdaUap = cmdBuf[10].toUByte()
                var bdaNap = cmdBuf[11].toUInt().and(0xff.toUInt()).shl(8) + cmdBuf[12].toUInt().and(0xff.toUInt())

                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)}  get hfp pair bda: ${bdaNap.toString(16)} : ${bdaUap.toString(16)} : ${bdaLap.toString(16)} ")
            }
            CmdId.SET_HFP_PAIR_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} set hfp pair bda")
            }
            CmdId.GET_HFP_FEATURE_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get hfp feature ${cmdBuf[6].toUInt().shl(8).and(0xffff.toUInt()).or(cmdBuf[7].toUInt()).toString(16)}")
            }
            CmdId.GET_AG_FEATURE_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get ag feature ${cmdBuf[6].toUInt().shl(8).and(0xffff.toUInt()).or(cmdBuf[7].toUInt()).toString(16)}")
            }
            CmdId.GET_HFP_PSKEY_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get hfp pskey id: ${cmdBuf[6].toUInt().shl(8).or(cmdBuf[7].toUInt())}")
            }
            CmdId.GET_AG_PSKEY_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get ag pskey id: ${cmdBuf[6].toUInt().shl(8).or(cmdBuf[7].toUInt())}")
            }
            CmdId.SET_HFP_PSKEY_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} set hfp pskey")
            }
            CmdId.SET_AG_PSKEY_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} set ag pskey")
            }
            CmdId.GET_SRC_VOL_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get src spkr vol: ${cmdBuf[7].toInt().and(0x0f)}, mic vol: ${cmdBuf[7].toInt().shr(4).and(0x0f)}")
            }
            CmdId.GET_AG_VOL_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get ag spkr vol: ${cmdBuf[7].toInt().and(0x0f)}, mic vol: ${cmdBuf[7].toInt().shr(4).and(0x0f)}")
            }
            CmdId.GET_HFP_RSSI_RSP.value -> {
                var rssi = cmdBuf[6].toInt().shl(8).or(cmdBuf[7].toInt())

                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get hfp rssi $rssi")
            }
            0xf0.toByte() -> {
                var str = when(cmdBuf[6]) {
                    0x0a.toByte() -> "STATE_OFF"
                    0x0b.toByte() -> "STATE_TURNING_ON"
                    0x0c.toByte() -> "STATE_ON"
                    0x0d.toByte() -> "STATE_TURNING_OFF"
                    0x0e.toByte() -> "STATE_BLE_TURNING_ON"
                    0x0f.toByte() -> "STATE_BLE_ON"
                    0x10.toByte() -> "STATE_BLE_TURNING_OFF"
                    else -> "STATE_UNKNOWN"
                }
                Log.d(KotlinclientReceiver,  "bluetooth state: $str")
            }
            0xf1.toByte() -> {
                var str : String
                if(cmdBuf[6] == 0x00.toByte())
                    str = "CONNECTED"
                else
                    str = "DISCONNECT"
                Log.d(KotlinclientReceiver,  "device: $btDevice connect state: $str")
            }
            0xf8.toByte() -> {
                var str : String
                if(cmdBuf[6] == 0x01.toByte())
                    str = "ENABLE"
                else
                    str = "DISABLE"
                Log.d(KotlinclientReceiver,  "bluetooth DISCOVERY: $str")
            }
            0xf9.toByte() -> {
                var bda = ""
                var name = ""
                var c : Char

                for(i in 0 .. 5) {
                    bda += cmdBuf[i + 7].toUByte().toString(16).toUpperCase() + ":"
                }
                bda = bda.trimEnd(':')
                for(i in 0 until  (cmdBuf[5] - 7) / 2) {
                    c = cmdBuf[i * 2 + 13].toInt().shl(8).and(0xff00).or(cmdBuf[i * 2 + 1 + 13].toInt().and(0x00ff)).toChar()
                    name += c
                }
                Log.d(KotlinclientReceiver, "\t\t\tdiscovery ==> RSSI: ${cmdBuf[6]} \taddress: $bda \tname: $name")
            }
            0xfa.toByte() -> {
                var bda = ""
                var name = ""
                var c : Char

                for(i in 0 .. 5) {
                    bda += cmdBuf[i + 7].toUByte().toString(16).toUpperCase() + ":"
                }
                bda = bda.trimEnd(':')
                for(i in 0 until  (cmdBuf[5] - 7) / 2) {
                    c = cmdBuf[i * 2 + 13].toInt().shl(8).and(0xff00).or(cmdBuf[i * 2 + 1 + 13].toInt().and(0x00ff)).toChar()
                    name += c
                }
                Log.d(KotlinclientReceiver, "\t\t\tbluetooth name changed ==> address: $bda \tname: $name")
            }
            0xfb.toByte() -> {
                var name = ""
                var c : Char

                for(i in 0 until  (cmdBuf[5] - 7) / 2) {
                    c = cmdBuf[i * 2 + 13].toInt().shl(8).and(0xff00).or(cmdBuf[i * 2 + 1 + 13].toInt().and(0x00ff)).toChar()
                    name += c
                }
                Log.d(KotlinclientReceiver, "\t\t\tlocal name changed ==> name: $name")
            }
            0xfc.toByte() -> {
                var str = when(cmdBuf[6]) {
                    0x0a.toByte() -> "BOND_NONE"
                    0x0b.toByte() -> "BOND_BONDING"
                    0x0c.toByte() -> "BOND_BONDED"
                    else -> "BOND_UNKNOWN"
                }
                Log.d(KotlinclientReceiver,  "device: $btDevice bond state: $str")
            }
            0xfd.toByte() -> {
                var str = when(cmdBuf[6]) {
                    0x14.toByte() -> "SCAN_MODE_NONE"
                    0x15.toByte() -> "SCAN_MODE_CONNECTABLE"
                    0x17.toByte() -> "SCAN_MODE_CONNECTABLE_DISCOVERABLE"
                    else -> "SCAN_MODE_UNKNOWN"
                }
                Log.d(KotlinclientReceiver,  "device: $btDevice scan mode state: $str")
            }
            else -> Log.d(KotlinclientReceiver, " other command data: ${cmdBuf[2].toUByte().toString(16)} ${cmdBuf[3].toUByte().toString(16)} ${cmdBuf[4].toUByte().toString(16)} ${cmdBuf[5].toUByte().toString(16)}")
        }
    }
}
