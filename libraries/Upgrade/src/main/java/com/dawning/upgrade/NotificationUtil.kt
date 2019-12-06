package com.dawning.upgrade

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import java.io.File


object NotificationUtil {

    private var manager: NotificationManager ?= null
    private fun getManager(context: Context): NotificationManager {
        if (manager == null) {
            manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return manager as NotificationManager
    }

    fun getNotificationBuilder(context: Context, icon: Int?, title: String?, content: String?, channelId: String): Notification.Builder {
        //大于8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //id随便指定
            val channel = NotificationChannel(channelId, context.getPackageName(), NotificationManager.IMPORTANCE_DEFAULT)
            channel.canBypassDnd()//可否绕过，请勿打扰模式
            channel.enableLights(true)//闪光
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET)//锁屏显示通知
            channel.setLightColor(Color.RED)//指定闪光是的灯光颜色
            channel.canShowBadge()//桌面laucher消息角标
            channel.enableVibration(true)//是否允许震动
            channel.setSound(null, null)
            //channel.getAudioAttributes();//获取系统通知响铃声音配置
            channel.getGroup()//获取通知渠道组
            channel.setBypassDnd(true)//设置可以绕过，请勿打扰模式
            channel.setVibrationPattern(longArrayOf(100, 100, 200))//震动的模式，震3次，第一次100，第二次100，第三次200毫秒
            channel.shouldShowLights()//是否会闪光
            //通知管理者创建的渠道
            getManager(context).createNotificationChannel(channel)

//            return NotificationCompat.Builder(context, channelId).setAutoCancel(true)
//                    .setContentTitle(title)
//                    .setContentText(content).setSmallIcon(icon!!)

            return  Notification.Builder(context, channelId)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setAutoCancel(true)
        }
        else {
            return Notification.Builder(context)
                    .setSmallIcon(icon!!)
                    .setContentText(content).setContentTitle( title )
        }


    }

    fun showNotification(context: Context, icon: Int?, title: String?, content: String?,
                                 manageId: Int, channelId: String, file: File) {
        val builder = getNotificationBuilder(context, icon, title, content, channelId)
        /* Intent intent = new Intent(this, SecondeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);*/
        builder.setOnlyAlertOnce(true)
        builder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
        builder.setWhen(System.currentTimeMillis())

        val intent = PendingIntent.getActivity(context, 0,
                UpgradeUtil.getInstallIntent(context, file), PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(intent)
        getManager(context).notify(manageId, builder.build())
    }

    fun showNotificationProgress(context: Context, icon: Int?, title: String?, content: String?,
                                 manageId: Int, channelId: String, progress: Int) {
        val builder = getNotificationBuilder(context, icon, title, content, channelId)
        /* Intent intent = new Intent(this, SecondeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);*/
        builder.setOnlyAlertOnce(true)
        builder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE)
        builder.setProgress(100, progress, false)
        builder.setWhen(System.currentTimeMillis())
        getManager(context).notify(manageId, builder.build())
    }

    fun cancleNotification(mContext: Context, manageId: Int) {
        getManager(mContext).cancel(manageId)
    }
}
