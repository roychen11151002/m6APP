package com.example.m6app_v001b001

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val KotlinclientReceiver = "kotlinReceiverTest"

class iMageBtClientReceiver : BroadcastReceiver() {

    @ExperimentalUnsignedTypes
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val btServiceData: iMageBtServiceData = intent.getParcelableExtra("btServiceData")
        // Log.d(KotlinclientReceiver, "client receive message ${btServiceData.btDeviceNo} ${btServiceData.btGroup}")
        var str = "\t\t\t\t\t\t\t group ${btServiceData.btGroup} device ${btServiceData.btDeviceNo}    "
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

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].btBase.conStatus = state
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].btBase.conStatus = state
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].btBase.conStatus = state
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].btBase.conStatus = state
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].btBase.conStatus = state
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].btBase.conStatus = state
                    0x30.toUByte() -> btM6Device.btSrcHfp.btBase.conStatus = state
                    else -> Log.d(KotlinclientReceiver, " HFP states get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} HFP status get ${state.toString(16)} ")
            }
            CmdId.GET_HFP_EXT_STA_RSP.value -> {
                var staExt = cmdBuf[6].toUInt().and(0xff.toUInt().shl(8).toUShort() + cmdBuf[7].toUShort().and(0xff.toUShort())).toUShort()

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].btBase.extStatus = staExt
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].btBase.extStatus = staExt
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].btBase.extStatus = staExt
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].btBase.extStatus = staExt
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].btBase.extStatus = staExt
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].btBase.extStatus = staExt
                    0x30.toUByte() -> btM6Device.btSrcHfp.btBase.extStatus = staExt
                    else -> Log.d(KotlinclientReceiver, " HFP extra states get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} HFP extra status get")
            }
            CmdId.GET_HFP_LOCAL_NAME_RSP.value -> {
                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x30.toUByte() -> btM6Device.btSrcHfp.btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    else -> Log.d(KotlinclientReceiver, " HFP local name get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} local name: ${String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())}")
            }
            CmdId.GET_AG_LOCAL_NAME_RSP.value -> {
                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x08.toUByte() -> btM6Device.btAg[0].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x10.toUByte() -> btM6Device.btAg[1].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x18.toUByte() -> btM6Device.btAg[1].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x20.toUByte() -> btM6Device.btAg[2].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x28.toUByte() -> btM6Device.btAg[2].btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x30.toUByte() -> btM6Device.btSrcHfp.btBase.localName = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    else -> Log.d(KotlinclientReceiver, " AGHFP local get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} local name: ${String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())}")
            }
            CmdId.GET_HFP_VRESION_RSP.value -> {
                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x30.toUByte() -> btM6Device.btSrcHfp.btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    else -> Log.d(KotlinclientReceiver, " HFP firmware version get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} firmware version: ${String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())}")
            }
            CmdId.GET_AG_VRESION_RSP.value -> {
                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x08.toUByte() -> btM6Device.btAg[0].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x10.toUByte() -> btM6Device.btAg[1].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x18.toUByte() -> btM6Device.btAg[1].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x20.toUByte() -> btM6Device.btAg[2].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x28.toUByte() -> btM6Device.btAg[2].btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    0x30.toUByte() -> btM6Device.btSrcHfp.btBase.firmwareVersion = String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())
                    else -> Log.d(KotlinclientReceiver, " AGHFP firmware version get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} firmware version: ${String(cmdBuf, 6, cmdBuf[5].toUByte().toInt())}")
            }
            CmdId.GET_SRC_DEV_NO_RSP.value -> {
                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].deviceNo = cmdBuf[6].toUByte()
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].deviceNo = cmdBuf[6].toUByte()
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].deviceNo = cmdBuf[6].toUByte()
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].deviceNo = cmdBuf[6].toUByte()
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].deviceNo = cmdBuf[6].toUByte()
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].deviceNo = cmdBuf[6].toUByte()
                    0x30.toUByte() -> btM6Device.btSrcHfp.deviceNo = cmdBuf[6].toUByte()
                    else -> Log.d(KotlinclientReceiver, " source number get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} source device number ${cmdBuf[6].toUByte().toString(16)}")
            }
            CmdId.GET_HFP_VOL_RSP.value -> {
                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].volHfp = cmdBuf[6].toUByte()
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].volHfp = cmdBuf[6].toUByte()
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].volHfp = cmdBuf[6].toUByte()
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].volHfp = cmdBuf[6].toUByte()
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].volHfp = cmdBuf[6].toUByte()
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].volHfp = cmdBuf[6].toUByte()
                    0x30.toUByte() -> btM6Device.btSrcHfp.volHfp = cmdBuf[6].toUByte()
                    else -> Log.d(KotlinclientReceiver, " HFP RSSI get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} volume ${cmdBuf[6].toUByte().toString(16)}")
            }
            CmdId.GET_HFP_PAIR_RSP.value -> {
                var bda = cmdBuf[11].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[12].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[10].toUByte().toString(16).toUpperCase() + ":" +
                        cmdBuf[7].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[8].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[9].toUByte().toString(16).toUpperCase()

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].btPairBda = bda
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].btPairBda = bda
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].btPairBda = bda
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].btPairBda = bda
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].btPairBda = bda
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].btPairBda = bda
                    0x30.toUByte() -> btM6Device.btSrcHfp.btPairBda = bda
                    else -> Log.d(KotlinclientReceiver, " HFP paired bt address get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)}  get hfp pair bda: ${bdaNap.toString(16)} : ${bdaUap.toString(16)} : ${bdaLap.toString(16)} ")
            }
            CmdId.SET_HFP_PAIR_RSP.value -> {
                Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} set hfp pair bda")
            }
            CmdId.GET_HFP_BDA_RSP.value -> {
                var bda = cmdBuf[11].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[12].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[10].toUByte().toString(16).toUpperCase() + ":" +
                        cmdBuf[7].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[8].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[9].toUByte().toString(16).toUpperCase()

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].btBase.btBda = bda
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].btBase.btBda = bda
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].btBase.btBda = bda
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].btBase.btBda = bda
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].btBase.btBda = bda
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].btBase.btBda = bda
                    0x30.toUByte() -> btM6Device.btSrcHfp.btBase.btBda = bda
                    else -> Log.d(KotlinclientReceiver, " hfp address get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)}  get hfp bda bda: ${bdaNap.toString(16)} : ${bdaUap.toString(16)} : ${bdaLap.toString(16)} ")
            }
            CmdId.GET_AG_BDA_RSP.value -> {
                var bda = cmdBuf[11].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[12].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[10].toUByte().toString(16).toUpperCase() + ":" +
                        cmdBuf[7].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[8].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[9].toUByte().toString(16).toUpperCase()

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btBase.btBda = bda
                    0x08.toUByte() -> btM6Device.btAg[0].btBase.btBda = bda
                    0x10.toUByte() -> btM6Device.btAg[1].btBase.btBda = bda
                    0x18.toUByte() -> btM6Device.btAg[1].btBase.btBda = bda
                    0x20.toUByte() -> btM6Device.btAg[2].btBase.btBda = bda
                    0x28.toUByte() -> btM6Device.btAg[2].btBase.btBda = bda
                    else -> Log.d(KotlinclientReceiver, " aghfp address get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)}  get aghfp bda bda: ${bdaNap.toString(16)} : ${bdaUap.toString(16)} : ${bdaLap.toString(16)} ")
            }
            CmdId.GET_HFP_FEATURE_RSP.value -> {
                var feature = (cmdBuf[6].toUShort().and(0xff.toUShort()).toUInt().shl(8).toUShort() + cmdBuf[7].toUShort().and(0xff.toUShort())).toUShort()
                var maxSlaveNo: UByte = cmdBuf[8].toUByte()
                var maxSlaveTalk: UByte = cmdBuf[9].toUByte()
                var bda: String = cmdBuf[15].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[16].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[14].toUByte().toString(16).toUpperCase() + ":" +
                        cmdBuf[11].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[12].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[13].toUByte().toString(16).toUpperCase()
                var led = ushortArrayOf((cmdBuf[17].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[18].toUShort().and(0xff.toUShort())).toUShort(), (cmdBuf[19].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[20].toUShort().and(0xff.toUShort())).toUShort(),
                    (cmdBuf[21].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[22].toUShort().and(0xff.toUShort())).toUShort(), (cmdBuf[23].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[24].toUShort().and(0xff.toUShort())).toUShort())

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> {
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.feature = feature
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.led = led
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.filterBda = bda
                    }
                    0x08.toUByte() -> {
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.feature = feature
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.led = led
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.filterBda = bda
                    }
                    0x10.toUByte() -> {
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.feature = feature
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.led = led
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.filterBda = bda
                    }
                    0x18.toUByte() -> {
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.feature = feature
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.led = led
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.filterBda = bda
                    }
                    0x20.toUByte() -> {
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.feature = feature
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.led = led
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.filterBda = bda
                    }
                    0x28.toUByte() -> {
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.feature = feature
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.led = led
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.filterBda = bda
                    }
                    0x30.toUByte() -> {
                        btM6Device.btSrcHfp.btBase.funFeature.feature = feature
                        btM6Device.btSrcHfp.btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btSrcHfp.btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btSrcHfp.btBase.funFeature.led = led
                        btM6Device.btSrcHfp.btBase.funFeature.filterBda = bda
                    }
                    else -> Log.d(KotlinclientReceiver, " HFP feature get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get hfp feature ${cmdBuf[6].toUInt().shl(8).and(0xffff.toUInt()).or(cmdBuf[7].toUInt()).toString(16)}")
            }
            CmdId.GET_AG_FEATURE_RSP.value -> {
                var feature: UShort = (cmdBuf[6].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[7].toUShort().and(0xff.toUShort())).toUShort()
                var maxSlaveNo: UByte = cmdBuf[8].toUByte()
                var maxSlaveTalk: UByte = cmdBuf[9].toUByte()
                var bda: String = cmdBuf[15].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[16].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[14].toUByte().toString(16).toUpperCase() + ":" +
                        cmdBuf[11].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[12].toUByte().toString(16).toUpperCase() + ":" + cmdBuf[13].toUByte().toString(16).toUpperCase()
                var led = ushortArrayOf((cmdBuf[17].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[18].toUShort().and(0xff.toUShort())).toUShort(), (cmdBuf[19].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[20].toUShort().and(0xff.toUShort())).toUShort(),
                    (cmdBuf[21].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[22].toUShort().and(0xff.toUShort())).toUShort(), (cmdBuf[23].toUInt().and(0xff.toUInt()).shl(8).toUShort() + cmdBuf[24].toUShort().and(0xff.toUShort())).toUShort())

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> {
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.feature = feature
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.led = led
                        btM6Device.btAg[0].btHfp[0].btBase.funFeature.filterBda = bda
                    }
                    0x08.toUByte() -> {
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.feature = feature
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.led = led
                        btM6Device.btAg[0].btHfp[1].btBase.funFeature.filterBda = bda
                    }
                    0x10.toUByte() -> {
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.feature = feature
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.led = led
                        btM6Device.btAg[1].btHfp[0].btBase.funFeature.filterBda = bda
                    }
                    0x18.toUByte() -> {
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.feature = feature
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.led = led
                        btM6Device.btAg[1].btHfp[1].btBase.funFeature.filterBda = bda
                    }
                    0x20.toUByte() -> {
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.feature = feature
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.led = led
                        btM6Device.btAg[2].btHfp[0].btBase.funFeature.filterBda = bda
                    }
                    0x28.toUByte() -> {
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.feature = feature
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.maxSlaveNo = maxSlaveNo
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.maxTalkNo = maxSlaveTalk
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.led = led
                        btM6Device.btAg[2].btHfp[1].btBase.funFeature.filterBda = bda
                    }
                    else -> Log.d(KotlinclientReceiver, " AGHFP feature get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get ag feature ${cmdBuf[6].toUInt().shl(8).and(0xffff.toUInt()).or(cmdBuf[7].toUInt()).toString(16)}")
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
                var vol = (cmdBuf[6].toUInt().and(0xff.toUInt()).shl(8) + cmdBuf[7].toUShort().and(0xff.toUShort())).toUShort()

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> {
                        btM6Device.btAg[0].volSrc[0] = vol
                    }
                    0x08.toUByte() -> {
                        btM6Device.btAg[0].volSrc[1] = vol
                    }
                    0x10.toUByte() -> {
                        btM6Device.btAg[1].volSrc[0] = vol
                    }
                    0x18.toUByte() -> {
                        btM6Device.btAg[1].volSrc[1] = vol
                    }
                    0x20.toUByte() -> {
                        btM6Device.btAg[2].volSrc[0] = vol
                    }
                    0x28.toUByte() -> {
                        btM6Device.btAg[2].volSrc[1] = vol
                    }
                    else -> Log.d(KotlinclientReceiver, " HFP RSSI get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get ag spkr vol: ${cmdBuf[7].toInt().and(0x0f)}, mic vol: ${cmdBuf[7].toInt().shr(4).and(0x0f)}")
            }
            CmdId.GET_HFP_RSSI_RSP.value -> {
                var rssi = (cmdBuf[6].toInt().shl(8).or(cmdBuf[7].toInt())).toShort()

                when(cmdBuf[2].toUByte()) {
                    0x00.toUByte() -> btM6Device.btAg[0].btHfp[0].rssi = rssi
                    0x08.toUByte() -> btM6Device.btAg[0].btHfp[1].rssi = rssi
                    0x10.toUByte() -> btM6Device.btAg[1].btHfp[0].rssi = rssi
                    0x18.toUByte() -> btM6Device.btAg[1].btHfp[1].rssi = rssi
                    0x20.toUByte() -> btM6Device.btAg[2].btHfp[0].rssi = rssi
                    0x28.toUByte() -> btM6Device.btAg[2].btHfp[1].rssi = rssi
                    0x30.toUByte() -> btM6Device.btSrcHfp.rssi = rssi
                    else -> Log.d(KotlinclientReceiver, " HFP RSSI get other source ${cmdBuf[2]}")
                }
                // Log.d(KotlinclientReceiver, " src ${cmdBuf[2].toUByte().toString(16)} get hfp rssi $rssi")
            }
            0xe1.toByte() -> {
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
            0xe3.toByte() -> {
                var str = when(cmdBuf[6]) {
                    0x00.toByte() -> "CONNECTED"
                    0x01.toByte() -> "DISCONNECT"
                    else -> "UNKNOWN"
                }
                Log.d(KotlinclientReceiver, "device: $btDevice connect state: $str")
            }
            0xe5.toByte() -> {
                var str = when(cmdBuf[6]) {
                    0x00.toByte() -> "STATE_DISCONNECT"
                    0x01.toByte() -> "STATE_CONNECTED"
                    else -> "STATE_UNKNOWN"
                }
                Log.d(KotlinclientReceiver,  "bluetooth connect state: $str")
            }
            0xe7.toByte() -> {
                var str = when(cmdBuf[6]) {
                    0x00.toByte() -> "DISABLE"
                    0x01.toByte() -> "ENABLE"
                    else -> "KNOWN"
                }
                Log.d(KotlinclientReceiver,  "bluetooth DISCOVERY: $str")
            }
            0xe9.toByte() -> {
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
            0xeb.toByte() -> {
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
            0xed.toByte() -> {
                var str = when(cmdBuf[6]) {
                    0x0a.toByte() -> "BOND_NONE"
                    0x0b.toByte() -> "BOND_BONDING"
                    0x0c.toByte() -> "BOND_BONDED"
                    else -> "BOND_UNKNOWN"
                }
                Log.d(KotlinclientReceiver,  "device: $btDevice bond state: $str")
            }
            0xef.toByte() -> {
                var str = when(cmdBuf[6]) {
                    0x14.toByte() -> "SCAN_MODE_NONE"
                    0x15.toByte() -> "SCAN_MODE_CONNECTABLE"
                    0x17.toByte() -> "SCAN_MODE_CONNECTABLE_DISCOVERABLE"
                    else -> "SCAN_MODE_UNKNOWN"
                }
                Log.d(KotlinclientReceiver,  "device: $btDevice scan mode state: $str")
            }
            0xf1.toByte() -> {
                var name = ""
                var c : Char

                for(i in 0 until  (cmdBuf[5] - 7) / 2) {
                    c = cmdBuf[i * 2 + 13].toInt().shl(8).and(0xff00).or(cmdBuf[i * 2 + 1 + 13].toInt().and(0x00ff)).toChar()
                    name += c
                }
                Log.d(KotlinclientReceiver, "\t\t\tlocal name changed ==> name: $name")
            }
            else -> Log.d(KotlinclientReceiver, " other command data: ${cmdBuf[2].toUByte().toString(16)} ${cmdBuf[3].toUByte().toString(16)} ${cmdBuf[4].toUByte().toString(16)} ${cmdBuf[5].toUByte().toString(16)}")
        }
    }
}
