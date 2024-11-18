package com.amastsales.uhf_r2_plugin.helper

import android.app.Activity
import android.util.Log
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.SystemClock

import com.rscja.deviceapi.RFIDWithUHFBLE
import com.rscja.deviceapi.interfaces.ScanBTCallback;

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amastsales.uhf_r2_plugin.helpers.MyDevice

import java.util.Collections
import kotlinx.coroutines.*

class Uhfr2Helper constructor() {
    private companion object {
        private var instance: Uhfr2Helper? = null
    }

    private var deviceList: MutableList<MyDevice> = ArrayList()
    private var devRssiValues: HashMap<String, Int> = HashMap()
    lateinit var mReader: RFIDWithUHFBLE
    lateinit var handler: Handler

    private var uhfListener: UhfR2Listener? = null

    private var isStart = false
    private var isConnect = false

    private var tagList: HashMap<String, EPC> = HashMap()

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

//    fun init() {
//        // this.context = context;
//        //this.uhfListener = uhfListener;
//        tagList = HashMap()
//        clearData()
//        handler = Handler()
//
////        {
////            @Override
////            fun handleMessage(msg: Message) {
////                String result = msg.obj + "";
////                String[] strs = result.split("@");
////                addEPCToList(strs[0], strs[1]);
////            }
////        }
//
//    }

    private fun isPermissionsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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

            val scanBTCallback = object : ScanBTCallback {
                override fun getDevices(
                    bluetoothDevice: BluetoothDevice?,
                    rssi: Int,
                    bytes: ByteArray
                ) {
                    var myDevice: MyDevice

                    if (bluetoothDevice != null) {
                        if (bluetoothDevice.name != null) {
                            myDevice = MyDevice(bluetoothDevice.address, bluetoothDevice.name, bluetoothDevice.bondState)
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
        this.mReader.stopScanBTDevices()
    }

    fun clearData() {
        tagList.clear()
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