package com.dawning.download

object Test {
    lateinit var downloadManager: DownloadManager
    fun get(): DownloadManager {

        downloadManager = DownloadManager()
        return downloadManager
    }
}
