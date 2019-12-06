package com.dawning.upgrade

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import java.io.File

class UpgradeManager {

    companion object {
        private var mUpgradeManager: UpgradeManager? = null

        val initialize: UpgradeManager
            get() {
                if (mUpgradeManager == null) {
                    mUpgradeManager = UpgradeManager()
                }
                return mUpgradeManager as UpgradeManager
            }
    }

    private var mBuilder: AlertDialog.Builder ?= null
    fun setBuilder(builder: AlertDialog.Builder): UpgradeManager {
        mBuilder = builder
        return this
    }

    private var mConfig: UpgradeConfig ?= null
    fun config(config: UpgradeConfig): UpgradeManager {
        mConfig = config
        return this
    }

    private var mLifecycle: Lifecycle ?= null
    fun setLifecycle(lifecycle: Lifecycle?): UpgradeManager {
        mLifecycle = lifecycle
        return this
    }

    private var inAdvance = false
    private var isCompleted = false
    fun check(context: Context, upgradeBean: UpgradeBean) {
        if (!UpgradeUtil.isNewVersion(context, upgradeBean)) {
            return
        }
        isCompleted = false
        if (mConfig == null) {
            mConfig = UpgradeConfig()
        }

        if (mConfig?.inAdvance!!) {
            inAdvance = true
            download(context, upgradeBean)
        }

        if (mBuilder == null) {
            val builder = UpgradeDialogBuilder(context, mConfig, upgradeBean)
            builder.setOnPositiveClickListener(View.OnClickListener {
                inAdvance = false
                download(context, upgradeBean)})
            mBuilder = builder
        }
        val dialog = mBuilder?.setCancelable(!upgradeBean.mustUpgrade)
                ?.show()
        dialog?.setOnDismissListener { mBuilder = null }
    }


    private fun download(context: Context, upgradeBean: UpgradeBean) {
        var hasPermission = false
        var dirFile: File = if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
            File(Environment.getExternalStorageDirectory().toString()
                    + File.separator + upgradeBean.dir)
        }
        else {
            File(context.cacheDir.toString() + File.separator + upgradeBean.dir)
        }
        if (!dirFile.exists()) dirFile.mkdirs()
        val file = File(dirFile.absolutePath + File.separator + upgradeBean.fileName)

        val upgradeListener = object : UpgradeListener {
            override fun progress(progress: Int) {
                if (mBuilder is UpgradeDialogBuilder) {
                    (mBuilder as UpgradeDialogBuilder)?.setProgress(progress)
                }

                if (isInAdvanceAndClick()) {
                    NotificationUtil.showNotificationProgress(context,
                            mConfig?.notificationIcon,
                            mConfig?.notificationTitle,
                            mConfig?.notificationDescription,
                            1,
                            "upgrade",
                            progress)
                }
            }

            override fun completed(file: File) {
                isCompleted = true
                if (!inAdvance) {
                    installAPK(context, file)
                    if (isInAdvanceAndClick()) {
                        NotificationUtil.cancleNotification(context, 1)
                        NotificationUtil.showNotification(context,
                                mConfig?.notificationIcon,
                                mConfig?.notificationTitle,
                                mConfig?.notificationDescription,
                                1,
                                "upgrade",
                                file)

                    }
                }
            }

            override fun error() {
            }
        }

        if (isInAdvanceAndClick() && !isCompleted) {
            return
        }

        if (file.exists()) {
            upgradeListener.completed(file)
            return
        }

        if (hasPermission) {
            val downloadId = UpgradeUtil.downloadExternal(context, mConfig, upgradeBean, file, inAdvance)
            if (mLifecycle != null) {
                val upgradeLifecycle = UpgradeLifecycle(context, downloadId, upgradeListener)
                mLifecycle?.addObserver(upgradeLifecycle)
            }
        }
        else {
            UpgradeUtil.downloadCache(context, mConfig, upgradeBean, file, inAdvance, upgradeListener)
        }
    }

    private fun isInAdvanceAndClick(): Boolean {
        return mConfig?.inAdvance!! && !inAdvance
    }

    private fun installAPK(context: Context, file: File) {
        context.startActivity(UpgradeUtil.getInstallIntent(context, file))
    }

}
