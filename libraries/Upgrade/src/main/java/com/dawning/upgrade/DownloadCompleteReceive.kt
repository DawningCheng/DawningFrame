package com.dawning.upgrade

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import java.io.File

class DownloadCompleteReceive(var upgradeListener: UpgradeListener) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

        val query = DownloadManager.Query().setFilterById(downloadId)
        var cursor: Cursor? = null
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            cursor = downloadManager.query(query)
            if (cursor != null && cursor!!.moveToFirst()) {
                var path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                if (path.contains("file://")) {
                    path = path.replace("file://", "")
                }
                upgradeListener.completed(File(path))
            }
        } finally {
            if (cursor != null) {
                cursor!!.close()
            }
        }
    }
}
