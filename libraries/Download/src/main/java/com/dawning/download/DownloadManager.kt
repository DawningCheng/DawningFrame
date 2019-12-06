package com.dawning.download


class DownloadManager {

    private var mDownloadManager: DownloadManager? = null

    internal val instance: DownloadManager
        get() {
            if (mDownloadManager == null) {
                mDownloadManager = DownloadManager()
            }
            return mDownloadManager as DownloadManager
        }

    fun download(path:String, url: String) {
    }



}
