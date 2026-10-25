package com.tizen.livetv.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.tizen.livetv.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class M3uImportDialog(
    context: Context,
    private val onM3uLoaded: (content: String) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_m3u_import)

        val etUrl = findViewById<EditText>(R.id.et_m3u_url)
        val btnCancel = findViewById<Button>(R.id.btn_m3u_cancel)
        val btnSave = findViewById<Button>(R.id.btn_m3u_save)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSave.setOnClickListener {
            val urlString = etUrl.text.toString().trim()
            if (urlString.isEmpty()) {
                Toast.makeText(context, "Masukkan URL playlist M3U", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Mengunduh..."

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val content = URL(urlString).readText()
                    withContext(Dispatchers.Main) {
                        onM3uLoaded(content)
                        Toast.makeText(context, "Berhasil memuat playlist!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        btnSave.isEnabled = true
                        btnSave.text = "Muat Saluran"
                        Toast.makeText(context, "Gagal mengunduh playlist: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
