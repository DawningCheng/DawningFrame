package com.dawning.upgrade

data class UpgradeConfig(
        var inAdvance: Boolean = false,
        val title: String = "升级",
        val content: String = "确认是否升级",
        val cancel: String = "取消",
        val affirm: String = "确认",
        val notificationIcon: Int = R.drawable.ic_launcher,
        val notificationTitle: String = "下载",
        val notificationDescription: String = "下载中") {

    constructor(inAdvance: Boolean) : this() {
        this.inAdvance = inAdvance
    }
}