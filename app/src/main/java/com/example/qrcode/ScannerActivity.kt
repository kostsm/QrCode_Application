package com.example.qrcode

import android.R.attr
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView
import android.content.Intent
import android.net.Uri
import com.google.zxing.client.result.URIResultParser
import android.R.bool
import android.app.AlertDialog
import android.app.Dialog
import android.util.Patterns
import android.net.ConnectivityManager
import androidx.fragment.app.DialogFragment
import android.content.IntentFilter
import android.content.ClipData
import android.R.attr.label
import android.content.ClipboardManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService

class ScannerActivity : AppCompatActivity(), ZBarScannerView.ResultHandler {
    private lateinit var zbView: ZBarScannerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zbView = ZBarScannerView(this)
        setContentView(zbView)
    }

    override fun onResume() {
        super.onResume()
        zbView.setResultHandler(this)
        zbView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        zbView.stopCamera()
    }

    override fun handleResult(result: Result?) {
        val isUrl: Boolean = Patterns.WEB_URL.matcher(result?.contents).matches()
        val resText: String? = result?.contents
        val url: String?
        val message: String?

        if (isUrl) {
            url = resText
            message = resText
        }
        else {
            url = "https://www.google.com/search?q=" + resText?.replace(" ", "%20")
            message = "Text: " + resText
        }

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        class MyDialogFragment : DialogFragment() {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return activity?.let {
                    val builder = AlertDialog.Builder(it)
                    builder.setTitle("Выберите действие")
                        .setMessage(message)
                        .setPositiveButton("Перейти по ссылке") {
                                dialog, id ->
                            startActivity(browserIntent)
                            finish()
                        }
                        .setNeutralButton("Назад") {
                                dialog, id -> dialog.cancel()
                            finish()
                        }
                        .setNegativeButton("Скопировать ссылку/текст") {
                                dialog, id ->
                            val clipboard: ClipboardManager =
                                getSystemService(context!!,ClipboardManager::class.java) as ClipboardManager
                            val clip = ClipData.newPlainText("", resText)
                            clipboard.setPrimaryClip(clip)
                            finish()
                        }
                    builder.create()
                } ?: throw IllegalStateException("Activity cannot be null")
            }
        }
        val myDialogFragment = MyDialogFragment()
        val manager = supportFragmentManager
        myDialogFragment.show(manager, "myDialog")
    }
}