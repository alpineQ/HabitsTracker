package com.alpineQ.habitstracker

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.SystemClock.sleep
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters

private const val TAG = "PollWorker"
private const val PREF_IS_POLLING = "isPolling"

class PollWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.i(TAG, "Work request triggered")
        val intent = MainActivity.newIntent(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val resources = applicationContext.resources
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            val notificationBuilder = NotificationCompat
                .Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setTicker(resources.getString(R.string.long_proccess_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.long_proccess_title))
                .setContentText(resources.getString(R.string.long_proccess_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, false)

            for (progress in 0..100) {
                notificationBuilder.setProgress(100, progress, false)
                notificationManager.notify(0, notificationBuilder.build())
                sleep(100)
            }

        } else {
            Toast.makeText(applicationContext, R.string.long_proccess_title, Toast.LENGTH_SHORT).show()
        }
        return Result.success()
    }

    companion object {
        fun isPolling(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_POLLING, false)
        }

        fun setPolling(context: Context, isOn: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putBoolean(PREF_IS_POLLING, isOn)
            }
        }
    }
}
