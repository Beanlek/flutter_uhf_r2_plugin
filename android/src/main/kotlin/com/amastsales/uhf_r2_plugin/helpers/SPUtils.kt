package com.amastsales.uhf_r2_plugin.helpers

import android.content.Context;
import android.content.SharedPreferences;

class SPUtils (var mInstance: SPUtils?, var sp: SharedPreferences?, context: Context) {
    companion object {
        val CURR_ADDRESS: String  = "currentAddress"

        val AUTO_RECONNECT: String  = "autoReconnect"
        val DISCONNECT_TIME: String  = "disconnectTime"
        val DISCONNECT_TIME_INDEX: String  = "disconnectTimeIndex"

        private fun getSP(context: Context): SharedPreferences {
            return context.getSharedPreferences("config", Context.MODE_PRIVATE)
        }
    }

    inner class Inner {

        fun getInstance(context: Context): SPUtils {
            if(mInstance == null) {
                synchronized (this) {
                    if(mInstance == null)
                    mInstance = SPUtils( null , null , context)
                }
            }
            return mInstance!!
        }

    }

    init {
        sp = getSP(context)
    }

    fun setSPString(key: String, value: String) {
        val editor: SharedPreferences.Editor = getEditor()
        editor.putString(key, value)
        editor.commit()
    }

    fun getSPString(key: String): String {
        return sp!!.getString(key, "")!!
    }

    fun setSPBoolean(key: String, value: Boolean): Boolean {
        val editor: SharedPreferences.Editor = getEditor()
        editor.putBoolean(key, value)
        return editor.commit()
    }

    fun getSPBoolean(key: String, defVal: Boolean): Boolean {
        return sp!!.getBoolean(key, defVal)
    }

    fun setSPInt(key: String, value: Int): Boolean {
        val editor: SharedPreferences.Editor = getEditor()
        editor.putInt(key, value)
        return editor.commit()
    }

    fun getSPInt(key: String, defVal: Int): Int {
        return sp!!.getInt(key, defVal)
    }

    fun setSPLong(key: String, value: Long): Boolean {
        val editor: SharedPreferences.Editor = getEditor()
        editor.putLong(key, value)
        return editor.commit()
    }

    fun getSPLong(key: String, defVal: Long): Long {
        return sp!!.getLong(key, defVal)
    }

    private fun getEditor(): SharedPreferences.Editor {
        return sp!!.edit();
    }
}