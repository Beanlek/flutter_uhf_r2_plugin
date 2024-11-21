package com.amastsales.uhf_r2_plugin.helper

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.util.Log
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.text.TextUtils
import android.widget.Toast

import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.interfaces.ScanBTCallback;

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.amastsales.uhf_r2_plugin.helpers.CheckUtils
import com.amastsales.uhf_r2_plugin.helpers.MyDevice
import com.amastsales.uhf_r2_plugin.helpers.SPUtils
import com.rscja.deviceapi.entity.UHFTAGInfo
import com.rscja.deviceapi.interfaces.ConnectionStatus
import com.rscja.deviceapi.interfaces.ConnectionStatusCallback

import java.util.Collections
import kotlinx.coroutines.*
import java.util.TimerTask

class Uhfr2Helper constructor() {
    private companion object {
        private var instance: Uhfr2Helper? = null

        const val TAG_DATA: String = "tagData"
        const val TAG_EPC: String = "tagEpc"
        const val TAG_TID: String = "tagTid"
        const val TAG_USER: String = "tagUser"
        const val TAG_LEN: String = "tagLen"
        const val TAG_COUNT: String = "tagCount"
        const val TAG_RSSI: String = "tagRssi"

        const val FLAG_START: Int = 0
        const val FLAG_STOP: Int = 1
        const val FLAG_UPDATE_TIME: Int = 2
        const val FLAG_UHFINFO: Int = 3
        const val FLAG_UHFINFO_LIST: Int = 5
        const val FLAG_SUCCESS: Int = 10
        const val FLAG_FAIL: Int = 11
        const val FLAG_TIME_OVER: Int= 12
    }

    private var deviceList: MutableList<MyDevice> = ArrayList()
    private var tempDatas: MutableList<UHFTAGInfo> = ArrayList()
    private var devRssiValues: HashMap<String, Int> = HashMap()

    lateinit var mReader: RFIDWithUHFBLE
    lateinit var mDevice: BluetoothDevice
    lateinit var mBluetoothAdapter: BluetoothAdapter
    lateinit var handler: Handler

    private var uhfListener: UhfR2Listener? = null
    private var btStatus: ConnectionStatusCallback<Any>? = null

    lateinit var mInventoryPerMinuteTask: TimerTask

    private var isConnect = false

//    private var tagList: HashMap<String, EPC> = HashMap()
    private var tagList: MutableList<HashMap<String, String>> = ArrayList()

    private fun addDevice(device: MyDevice, rssi: Int): MutableList<MyDevice> {
        var deviceFound: Boolean = false;

        if(device.name == null || device.name.equals("")) {
            return deviceList
        }

        for (listDev: MyDevice in deviceList) {
            if (listDev.address.equals(device.address)) {
                deviceFound = true;
                break;
            }
        }
        devRssiValues.put(device.address, rssi);
        if (!deviceFound) {
            Log.d("device.name", device.name)
            Log.d("device.address", device.address)
            Log.d("device.bondState", device.bondState.toString())

            deviceList.add(device);
        }

        val comparatorMyDevice = object : Comparator<MyDevice> {
            override fun compare(
                device1: MyDevice,
                device2: MyDevice
            ): Int {
                var key1: String = device1.address;
                var key2: String = device1.address;

                var v1: Int = devRssiValues.get(key1)!!
                var v2: Int = devRssiValues.get(key2)!!

                return if (v1 > v2) {
                    -1;
                } else if (v1 < v2) {
                    1;
                } else {
                    0;
                }
            }

        }

        // Reorder based on signal strength
        Collections.sort(deviceList, comparatorMyDevice);

        return deviceList
    }

    fun getInstance(): Uhfr2Helper {
        if (instance == null) {
            instance = Uhfr2Helper()
        }
        return instance!!
    }

    fun setUhfListener(uhfListener: UhfR2Listener) {
        this.uhfListener = uhfListener
    }

    private fun isPermissionsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            return true
        } else {
            return false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun startScan(context: Context, activity: Activity): MutableList<MyDevice> {
        init(context)

        fun mReaderStarScanning(scanBTCallback: ScanBTCallback): Deferred<Boolean> = GlobalScope.async(){
            mReader.startScanBTDevices(scanBTCallback)
            SystemClock.sleep(2000)
            mReader.stopScanBTDevices()

            return@async true
        }


        if (!isPermissionsGranted(context)) {
            val permissions = mutableSetOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
                permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            }

            ActivityCompat.requestPermissions(
                activity, permissions.toTypedArray(), 600
            )
        } else {
            Log.d("isPermissionGranted", "true")

            if (!isBluetoothEnabled()) {
                val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(context, discoverableIntent, null)

                if (!isBluetoothEnabled()) {
                    return deviceList
                }
            }

            val scanBTCallback = object : ScanBTCallback {
                override fun getDevices(
                    bluetoothDevice: BluetoothDevice?,
                    rssi: Int,
                    bytes: ByteArray
                ) {
                    var myDevice: MyDevice

                    if (bluetoothDevice != null) {
                        if (bluetoothDevice.name != null) {
                            myDevice = MyDevice(bluetoothDevice.address, bluetoothDevice.name, bluetoothDevice.bondState, rssi)
                            deviceList = addDevice(myDevice, rssi)
                        } else return
                    } else return

                    return
                }
            }

            val done = mReaderStarScanning(scanBTCallback)
            done.await()
        }


        Log.d("deviceList", deviceList.toString())


        return deviceList

    }

    fun stopScan() {
        mReader.stopScanBTDevices()
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun connect(context: Context, deviceAddress: String): Int {
        init(context)

        fun mReaderConnect(deviceAddress: String, btStatus: ConnectionStatusCallback<Any>?): Deferred<Boolean> = GlobalScope.async() {
            Log.d("mReaderConnect", "isRunning ${deviceAddress}")

            val toConnect = async(start = CoroutineStart.LAZY) { mReader.connect(deviceAddress, btStatus!!) }
            toConnect.start()
            toConnect.await()

            var connected: Boolean = false

            while (true) {
                if (mReader.getConnectStatus() != ConnectionStatus.CONNECTING) {
                    delay(2000)
                    connected = mReader.getConnectStatus() == ConnectionStatus.CONNECTED
                    break
                }
            }
            if (connected) {
                SPUtils(null, null, context).Inner().getInstance(context).setSPString(SPUtils.CURR_ADDRESS, deviceAddress);
            }

            Log.d("getConnectStatus", connected.toString())

            return@async connected
        }

        try {
            btStatus = BTStatus()

            var currDevice: String = SPUtils(null, null, context).Inner().getInstance(context).getSPString(SPUtils.CURR_ADDRESS);

            if (mReader.getConnectStatus() == ConnectionStatus.CONNECTED) {
                disconnect()

                if (currDevice == deviceAddress) {
                    return 3
                }
            }

            Log.d("deviceAddress", deviceAddress)

            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress)
            Log.d("mDevice instance", mDevice.toString())

            val done = mReaderConnect(deviceAddress, btStatus!!)
            val status: Boolean = done.await()
            Log.d("connect status", status.toString())

            if (status) {
                return 1
            } else {
                return 2
            }

        } catch (e: Exception) {
            Log.d("Internal Error", e.toString())

            return 2
        }


//        if (mReader.getConnectStatus() == ConnectionStatus.CONNECTING) {
//            Log.d("connect", "Connecting")
//        } else {
//            mReader.connect(deviceAddress, btStatus!!)
//        }

    }

    fun getConnectionStatus():String {
        if (this::mReader.isInitialized) {
            if (mReader.getConnectStatus() == ConnectionStatus.CONNECTING) {
                return "CONNECTING"
            } else if (mReader.getConnectStatus() == ConnectionStatus.CONNECTED) {
                return "CONNECTED"
            } else if (mReader.getConnectStatus() == ConnectionStatus.DISCONNECTED) {
                return "DISCONNECTED"
            } else {
                return "ERROR"
            }

        } else {
            return "NOT INITIALIZED"
        }

    }

    private fun addEPCToList(list: List<UHFTAGInfo>) {
        for((k, value) in list.withIndex()){
            addEPCToList(list.get(k));
        }
    }

    private fun addEPCToList(uhftagInfo: UHFTAGInfo ) {
        var exists: BooleanArray = BooleanArray(1)
        val immutableTempData: List<UHFTAGInfo> = tempDatas.toList()

        var idx: Int = CheckUtils.getInsertIndex(immutableTempData, uhftagInfo, exists)

        insertTag(uhftagInfo,idx,exists[0])
    }

    private fun insertTag(info: UHFTAGInfo , index: Int, exists: Boolean) {

        var data: String = info.getEPC()

        if(!TextUtils.isEmpty(info.getTid())){
            var stringBuilder: StringBuilder  = StringBuilder();

            stringBuilder.append("EPC:")
            stringBuilder.append(info.getEPC())
            stringBuilder.append("\n")
            stringBuilder.append("TID:")
            stringBuilder.append(info.getTid())

            if(!TextUtils.isEmpty(info.getUser())){
                stringBuilder.append("\n")
                stringBuilder.append("USER:")
                stringBuilder.append(info.getUser())
            }

            data = stringBuilder.toString()
        }

        var tagMap: HashMap<String,String> = HashMap()

        if(exists){
            tagMap = tagList.get(index)
            tagMap.put(TAG_COUNT, ((Integer.parseInt(tagMap.get(TAG_COUNT)!!)+1).toString() ))
        } else {

            tagMap.put(TAG_EPC, info.getEPC())
            tagMap.put(TAG_COUNT, "1")
            tempDatas.add(index, info)
            tagList.add(index, tagMap)
        }
        tagMap.put(TAG_USER, info.getUser())
        tagMap.put(TAG_DATA, data)
        tagMap.put(TAG_TID, info.getTid())
        tagMap.put(TAG_RSSI, info.getRssi()!!)
    }

    fun tagSingle(): MutableList<HashMap<String, String>> {
        var mStrTime: Long = System.currentTimeMillis()

        this.handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    FLAG_TIME_OVER -> {
//                        Log.i(TAG, "FLAG_TIME_OVER =" + (System.currentTimeMillis() - mStrTime))
//                        var useTime2: Float =(System.currentTimeMillis() - mStrTime) / 1000.0F
//
                        Log.d("FLAG_TIME_OVER", (System.currentTimeMillis() - mStrTime).toString())

                        stop()

                    }

                    FLAG_STOP -> {
                        if (msg.arg1 == FLAG_SUCCESS) {
                            Log.d("FLAG_SUCCESS", "true")
                        } else {
                            Log.d("FLAG_SUCCESS", "false")
                        }

                    }

                    FLAG_UHFINFO_LIST -> {
                        var list: List<UHFTAGInfo> = msg.obj as List<UHFTAGInfo>
                        addEPCToList(list);

                    }

                    FLAG_START -> {
                        if (msg.arg1 == FLAG_SUCCESS) {
                            Log.d("FLAG_SUCCESS", "true")
                        } else {
                            Log.d("FLAG_SUCCESS", "false")
                        }

                    }

                    FLAG_UPDATE_TIME -> {
//                        if (mContext.isScanning) {
//                            float useTime =(System.currentTimeMillis() - mStrTime) / 1000.0F;
//                            tv_time.setText(NumberTool.getPointDouble(1, useTime) + "s");
//                            handler.sendEmptyMessageDelayed(FLAG_UPDATE_TIME, 10);
//                        } else {
//                            handler.removeMessages(FLAG_UPDATE_TIME);
//                        }
                        handler.removeMessages(FLAG_UPDATE_TIME);

                    }
                    FLAG_UHFINFO -> {
                        var info: UHFTAGInfo = msg.obj as UHFTAGInfo;
                        addEPCToList(info);

                    }
                }
            }
        }

        try {
            var info: UHFTAGInfo  = mReader.inventorySingleTag()!!

            if (info != null) {
                var msg: Message = handler.obtainMessage(FLAG_UHFINFO)
                msg.obj = info;
                handler.sendMessage(msg);
            }

            handler.sendEmptyMessage(FLAG_UPDATE_TIME);
        } catch (e: Exception) {
            mReader.stopInventory()
            Log.d("No Tag", e.toString())
        }

        SystemClock.sleep(100)

        Log.d("Taglist", tagList.toString())

        return tagList
    }

    fun stop() {
        handler.removeMessages(FLAG_TIME_OVER);
        cancelInventoryTask();
    }

    private fun cancelInventoryTask() {
        if(this::mInventoryPerMinuteTask.isInitialized) {
            mInventoryPerMinuteTask.cancel();
        }
    }

    fun disconnect() {
        mReader.disconnect()
    }

    fun clearData() {
        tagList.clear()
        tempDatas.clear()
    }

    fun init(context: Context): Boolean {
        try {
            this.mReader = RFIDWithUHFBLE.getInstance()
        } catch (ex: Exception) {
            uhfListener!!.onConnect(false, 0)
            return false
        }

        if (mReader != null) {
            isConnect = this.mReader.init(context)

            Log.d("mReader init", isConnect.toString())

            uhfListener!!.onConnect(isConnect, 0)
            return isConnect
        }

        uhfListener!!.onConnect(false, 0)
        return false
    }

    fun isConnected(): Boolean {
        return isConnect
    }

}

class BTStatus: ConnectionStatusCallback<Any> {
//    val uhfr2Helper: Uhfr2Helper = Uhfr2Helper()

    override fun getStatus(connectionStatus: ConnectionStatus, device1: Any?) {

//        suspend fun run(mDevice: BluetoothDevice?, context: Context) {
//            Log.d("BTStatus", "isRunning")
//
//            val device: BluetoothDevice = device1 as BluetoothDevice;
//            Log.d("BTStatus device", device.toString())
//
//            var remoteBTName = "";
//            var remoteBTAdd = "";
//
//            if (connectionStatus == ConnectionStatus.CONNECTED) {
//
//                remoteBTName = device.getName();
//                remoteBTAdd = device.getAddress();
//
//            } else if (connectionStatus == ConnectionStatus.DISCONNECTED) {
//
//                remoteBTName = device.getName();
//                remoteBTAdd = device.getAddress();
//
//                var reconnect: Boolean = SPUtils(null, null, context).Inner().getInstance(context).getSPBoolean(SPUtils.AUTO_RECONNECT, false)
//
//                if (mDevice != null && reconnect) {
//                    uhfr2Helper.connect(context, remoteBTAdd)
//                }
//            }
//
//        }
    }
}
