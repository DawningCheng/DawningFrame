package com.dawning.upgrade

data class UpgradeBean(val versionName: String, val versionCode: Int, val url: String,
                       val dir: String, val fileName: String, val mustUpgrade: Boolean)
