package com.amastsales.uhf_r2_plugin.helper

interface UhfR2Listener {
    fun onRead(tagsJson: String)

    fun onConnect(isConnected: Boolean, powerLevel: Int)
}