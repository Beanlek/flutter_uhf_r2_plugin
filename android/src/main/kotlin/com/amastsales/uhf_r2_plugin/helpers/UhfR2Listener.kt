package com.amastsales.uhf_r2_plugin.helper

interface UhfR2Listener {
//    private companion object {
//        private var instance: UhfR2Listener? = null
//    }

    fun onRead(tagsJson: String)

    fun onConnect(isConnected: Boolean, powerLevel: Int)
}