package com.dudek.dicodingstory.data.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.dudek.dicodingstory.data.pref.SessionPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TokenBackgroundService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "TokenBackgroundService dijalankan...")

        serviceScope.launch {
            monitorToken()
        }

        return START_STICKY
    }

    private suspend fun monitorToken() {
        val sessionPreference = SessionPreference(this)
        sessionPreference.token.collect { token ->
            if (token == null) {
                Log.d(TAG, "Token is null. Stopping service.")
                stopSelf()
            } else {
                Log.d(TAG, "Token is valid: $token")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "TokenBackgroundService dihentikan")
    }

    companion object {
        const val TAG = "TokenBackgroundService"

        fun startService(context: Context) {
            val intent = Intent(context, TokenBackgroundService::class.java)
            context.startService(intent)
        }
    }
}