package com.dawning.upgrade

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

class UpgradeLifecycle(var context: Context, downloadId: Long, upgradeListener: UpgradeListener) : LifecycleObserver {

//    private var mDownloadCompleteReceive: DownloadCompleteReceive ?= null
    private var mDownloadChangeObserver: DownloadChangeObserver ?= null
    init {
//        mDownloadCompleteReceive = DownloadCompleteReceive(upgradeListener)
//        context.registerReceiver(mDownloadCompleteReceive, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        mDownloadChangeObserver = DownloadChangeObserver(context, downloadId, upgradeListener)
        context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"),
                true, mDownloadChangeObserver!!)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroy(owner: LifecycleOwner) {
//        context.unregisterReceiver(mDownloadCompleteReceive)
        context.getContentResolver().unregisterContentObserver(mDownloadChangeObserver!!)
    }


}
