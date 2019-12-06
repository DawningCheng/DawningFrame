package com.dawning.upgrade

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.helian.app.download.DownloadHelper
import java.io.File

object UpgradeUtil {

    /**
     * 此方法是通过将版本信息拆分成数据的形式进行比较
     *
     * @param version
     * @return
     */
    fun isNewVersion(context: Context, upgradeBean: UpgradeBean): Boolean {
        val versionName = upgradeBean.versionName
        val versionCode = upgradeBean.versionCode
        val pm = context.packageManager;
        val packageInfo = pm.getPackageInfo(context.packageName, 0);
        val currentVersionName = packageInfo.versionName
        val currentVersionCode = packageInfo.versionCode
        if (!TextUtils.isEmpty(versionName) && !TextUtils.isEmpty(currentVersionName)) {
            val newVer = versionName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val oldVer = currentVersionName.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val size = Math.min(newVer.size, oldVer.size)
            for (i in 0 until size) {
                try {
                    if (Integer.valueOf(newVer[i]) > Integer.valueOf(oldVer[i]))
                        return true
                    else if (Integer.valueOf(newVer[i]) < Integer.valueOf(oldVer[i]))
                        return false
                } catch (exception: NumberFormatException) {
                    return false
                }

            }
            if (newVer.size > oldVer.size)
                return true
        }
        else if (versionCode > 0 && versionCode > currentVersionCode) {
            return true
        }
        return false
    }

    fun downloadExternal(context: Context, config: UpgradeConfig?, upgradeBean: UpgradeBean,
                         file: File, inAdvance: Boolean): Long {
        val request = DownloadManager.Request(Uri.parse(upgradeBean.url))

        if (!inAdvance) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        else {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        }

        //设置通知栏的标题
        request.setTitle(config?.notificationTitle)
        //设置通知栏的message
        request.setDescription(config?.notificationDescription)
        //设置文件存放目录

        request.setDestinationUri(file.toUri())
//        request.setDestinationInExternalFilesDir(context, upgradeBean.dir, upgradeBean.fileName)
        //获取系统服务
        val downloadManager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        //进行下载
        return downloadManager.enqueue(request)
    }

//    private fun receiveStatus(context: Context, receiver: BroadcastReceiver,
//                              downloadManager: DownloadManager, id: Long) {
//        val query = DownloadManager.Query()
//        //通过下载的id查找
//        query.setFilterById(id)
//        val cursor = downloadManager.query(query)
//        if (cursor.moveToFirst()) {
//            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
//            when (status) {
//                //下载暂停
//                DownloadManager.STATUS_PAUSED -> {
//                }
//                //下载延迟
//                DownloadManager.STATUS_PENDING -> {
//                }
//                //正在下载
//                DownloadManager.STATUS_RUNNING -> {
//                    //已经下载的字节数
//                    val byte = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//                    //总需下载的字节数
//                    val total = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//                }
//                //下载完成
//                DownloadManager.STATUS_SUCCESSFUL -> {
//                    //下载完成安装APK
//                    cursor.close()
//                    context.unregisterReceiver(receiver)
//                }
//                //下载失败
//                DownloadManager.STATUS_FAILED -> {
//                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
//                    cursor.close()
//                    context.unregisterReceiver(receiver)
//                }
//            }
//        }
//    }

    fun downloadCache(context: Context, config: UpgradeConfig?, upgradeBean: UpgradeBean, file: File,
                      inAdvance: Boolean, upgradeListener: UpgradeListener) {
        var manageId = 0
        if (!inAdvance) {
            manageId = 1
            NotificationUtil.showNotificationProgress(context,
                    config?.notificationIcon,
                    config?.notificationTitle,
                    config?.notificationDescription,
                    manageId,
                    "upgrade",
                    0)
        }

        DownloadHelper.getInstance().download(context, upgradeBean.url,
                file.absolutePath,
                object : DownloadHelper.DownloadListener {
                    override  fun progress(progress: Int) {
                        upgradeListener.progress(progress)
                        Log.e("downloadCache", "" + progress)
                        if (manageId > 0) {
                            NotificationUtil.showNotificationProgress(context,
                                    config?.notificationIcon,
                                    config?.notificationTitle,
                                    config?.notificationDescription,
                                    manageId,
                                    "upgrade",
                                    progress)
                        }
                    }

                    override  fun completed() {
                        if (manageId > 0) {
                            NotificationUtil.cancleNotification(context, manageId)
                            NotificationUtil.showNotification(context,
                                    config?.notificationIcon,
                                    config?.notificationTitle,
                                    config?.notificationDescription,
                                    manageId,
                                    "upgrade",
                                    file)
                        }

                        if (file.exists()) {
                            upgradeListener.completed(file)
                        }
                    }

                    override fun error() {
                        upgradeListener.error()
                    }
                })
    }

    fun getInstallIntent(context: Context, file: File?): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //判断是否是AndroidN以及更高的版本
        var uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            FileProvider.getUriForFile(context, "com.dawning.app.fileProvider", file!!)
        } else {
            Uri.fromFile(file)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        return intent
    }

}
