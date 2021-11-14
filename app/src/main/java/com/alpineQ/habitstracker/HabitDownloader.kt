package com.alpineQ.habitstracker

import android.annotation.SuppressLint
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "HabitDownloader"
private const val MESSAGE_DOWNLOAD = 0

class HabitDownloader(private val responseHandler: Handler,
                            private val onHabitDownloaded: (UUID, Habit) -> Unit
) : HandlerThread(TAG), LifecycleObserver {
    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<UUID, String>()

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as UUID
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun setup() {
        Log.i(TAG, "Starting background thread")
        start()
        looper
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "Destroying background thread")
        quit()
    }

    fun queueHabit(target: UUID, title: String) {
        Log.i(TAG, "Got a habit: $title")
        requestMap[target] = title
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
            .sendToTarget()
    }

    private fun handleRequest(target: UUID) {
        val title = requestMap[target] ?: return
        val habit = Habit(title=title)
        responseHandler.post(Runnable {
            if (requestMap[target] != title || hasQuit) {
                return@Runnable
            }
            requestMap.remove(target)
            onHabitDownloaded(target, habit)
        })
    }


}