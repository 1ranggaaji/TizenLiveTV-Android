package com.tizen.livetv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tizen.livetv.ui.MainActivity

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received boot broadcast: $action")

        val prefs = context.getSharedPreferences("tizen_live_tv_prefs", Context.MODE_PRIVATE)
        val isAutoLaunchEnabled = prefs.getBoolean("pref_auto_launch_on_boot", true)

        if (!isAutoLaunchEnabled) {
            Log.d("BootReceiver", "Auto-launch on boot is disabled by user.")
            return
        }

        if (Intent.ACTION_BOOT_COMPLETED == action ||
            "android.intent.action.QUICKBOOT_POWERON" == action ||
            "com.htc.intent.action.QUICKBOOT_POWERON" == action
        ) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("EXTRA_BOOT_STARTED", true)
            }
            try {
                context.startActivity(launchIntent)
                Log.d("BootReceiver", "Successfully launched Live TV on boot.")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to launch Live TV on boot: ${e.message}")
            }
        }
    }
}
