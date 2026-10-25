package com.tizen.livetv.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.tizen.livetv.R

class QuickSettingsDialog(
    context: Context,
    private val onCycleAspectRatio: () -> String,
    private val onResetChannels: () -> Unit
) : Dialog(context) {

    private val prefs = context.getSharedPreferences("tizen_live_tv_prefs", Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_quick_settings)

        val btnRatio = findViewById<Button>(R.id.btn_setting_ratio)
        val btnBoot = findViewById<Button>(R.id.btn_setting_boot)
        val btnLauncher = findViewById<Button>(R.id.btn_setting_launcher)
        val btnReset = findViewById<Button>(R.id.btn_setting_reset)
        val btnClose = findViewById<Button>(R.id.btn_setting_close)

        // Setup Boot toggle
        var isAutoBoot = prefs.getBoolean("pref_auto_launch_on_boot", true)
        updateBootButtonText(btnBoot, isAutoBoot)

        btnBoot.setOnClickListener {
            isAutoBoot = !isAutoBoot
            prefs.edit().putBoolean("pref_auto_launch_on_boot", isAutoBoot).apply()
            updateBootButtonText(btnBoot, isAutoBoot)
            Toast.makeText(
                context,
                if (isAutoBoot) "Auto-launch TV Aktif saat booting" else "Auto-launch TV Dinonaktifkan",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnRatio.setOnClickListener {
            val modeName = onCycleAspectRatio()
            btnRatio.text = modeName
        }

        btnLauncher.setOnClickListener {
            try {
                // Open Android TV Home settings or App settings
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    Toast.makeText(context, "Buka Pengaturan Android TV -> Aplikasi Default -> Beranda", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnReset.setOnClickListener {
            onResetChannels()
            Toast.makeText(context, "Daftar saluran berhasil dikembalikan ke standar", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun updateBootButtonText(button: Button, isAuto: Boolean) {
        button.text = if (isAuto) {
            "Buka Otomatis Saat TV Menyala: AKTIF"
        } else {
            "Buka Otomatis Saat TV Menyala: NONAKTIF"
        }
    }
}
