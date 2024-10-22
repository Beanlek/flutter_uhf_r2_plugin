package com.amastsales.uhf_r2_plugin.helper

class EPC {
    private var count: String = "n/a"
    private var epc: String = "n/a"
    private var id: String = "n/a"
    private var rssi: String = "n/a"

    private var isFind: Boolean = false

    fun isFind(): Boolean {
        return this.isFind
    }

    fun setFind(isFind2: Boolean) {
        this.isFind = isFind2
    }

    fun getId(): String {
        return this.id
    }

    fun setId(id2: String?) {
        this.id = id2!!
    }

    fun getEpc(): String {
        return this.epc
    }

    fun setEpc(epc2: String?) {
        this.epc = epc2!!
    }

    fun getCount(): String {
        return this.count
    }

    fun setCount(count2: String?) {
        this.count = count2!!
    }

    fun getRssi(): String {
        return this.rssi
    }

    fun setRssi(rssi2: String?) {
        this.rssi = rssi2!!
    }

    override fun toString(): String {
        return "EPC [id=" + this.id + ", epc=" + this.epc + ", count=" + this.count + "]"
    }
}