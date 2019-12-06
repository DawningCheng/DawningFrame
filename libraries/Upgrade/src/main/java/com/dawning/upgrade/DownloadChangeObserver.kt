package com.dawning.upgrade

import android.app.DownloadManager
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.os.Handler
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DownloadChangeObserver(var context: Context, var downloadId: Long, var upgradeListener: UpgradeListener) : ContentObserver(Handler()) {

    private var scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    /**
     * 当所监听的Uri发生改变时，就会回调此方法
     *
     * @param selfChange 此值意义不大, 一般情况下该回调值false
     */
    override fun onChange(selfChange: Boolean) {
        if (!scheduledExecutorService.isShutdown) {
            scheduledExecutorService.scheduleAtFixedRate( { getBytesAndStatus(downloadId) }, 0, 2, TimeUnit.SECONDS)
        }
    }

    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     *
     * @param downloadId
     * @return
     */
    private fun getBytesAndStatus(downloadId: Long): IntArray {
        val bytesAndStatus = intArrayOf(-1, -1, 0)
        val query = DownloadManager.Query().setFilterById(downloadId)
        var cursor: Cursor? = null
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            cursor = downloadManager.query(query)
            if (cursor != null && cursor!!.moveToFirst()) {
                when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    //正在下载
                    DownloadManager.STATUS_RUNNING -> {
                        //已经下载文件大小
                        bytesAndStatus[0] = cursor!!.getInt(cursor!!.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        //下载文件的总大小
                        bytesAndStatus[1] = cursor!!.getInt(cursor!!.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        //下载状态
                        bytesAndStatus[2] = cursor!!.getInt(cursor!!.getColumnIndex(DownloadManager.COLUMN_STATUS))

                        val progress = (bytesAndStatus[0].toFloat() / bytesAndStatus[1] * 100).toInt()
                        if (progress != 100) {
                            upgradeListener.progress(progress)
                        }
                        else {
                            scheduledExecutorService.shutdown()
                        }
                    }
                    //下载完成
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        //下载完成安装APK
                        close(cursor)
                        var path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                        if (path.contains("file://")) {
                            path = path.replace("file://", "")
                        }
                        upgradeListener.completed(File(path))
                    }
                    else -> {
                        close(cursor)
                        upgradeListener.error()
                    }
//                    //下载暂停
//                    DownloadManager.STATUS_PAUSED -> {
//                    }
//                    //下载延迟
//                    DownloadManager.STATUS_PENDING -> {
//                    }
//                    //下载失败
//                    DownloadManager.STATUS_FAILED -> {
//                        cursor.close()
//                        scheduledExecutorService.shutdown()
//                        upgradeListener.error()
//                    }
                }

            }
        } finally {
            if (cursor != null) {
                cursor!!.close()
            }
        }
        return bytesAndStatus
    }

    private fun close(cursor: Cursor) {
        cursor.close()
        scheduledExecutorService.shutdown()
        scheduledExecutorService = null
        context.getContentResolver().unregisterContentObserver(this)
    }

}
