package com.amastsales.uhf_r2_plugin.helper

import android.content.Context
import android.os.Handler
import android.util.Log
import com.rscja.deviceapi.RFIDWithUHFBLE

class Uhfr2Helper constructor() {
    private companion object {
        private var instance: Uhfr2Helper? = null
    }

    var mReader: RFIDWithUHFBLE? = null

    private var uhfListener: UhfR2Listener? = null
    private var isStart = false
    private var isConnect = false
    private val tagList: HashMap<String, EPC> = HashMap()

    fun getInstance(): Uhfr2Helper {
        if (instance == null) {
            instance = Uhfr2Helper()
        }
        return instance!!
    }

    fun connect(context: Context): Boolean {
        try {
            mReader = RFIDWithUHFBLE.getInstance()
        } catch (ex: Exception) {
            uhfListener!!.onConnect(false, 0)
            return false
        }

        if (mReader != null) {
            isConnect = mReader!!.init(context)
            Log.d("mReader init", isConnect.toString())
//            uhfListener!!.onConnect(isConnect, 0)
            return isConnect
        }
        uhfListener!!.onConnect(false, 0)
        return false
    }

    fun isConnected(): Boolean {
        return isConnect
    }
}