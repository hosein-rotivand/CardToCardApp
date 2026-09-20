package com.example.cardtocard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // اگر قبلا لاگین کرده، مستقیم برو صفحه اصلی
        if (Prefs.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val editSiteUrl = findViewById<EditText>(R.id.edit_site_url)
        val editPhone = findViewById<EditText>(R.id.edit_phone)
        val editPassword = findViewById<EditText>(R.id.edit_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val textError = findViewById<TextView>(R.id.text_error)
        val progress = findViewById<ProgressBar>(R.id.progress)

        btnLogin.setOnClickListener {
            val siteUrl = editSiteUrl.text.toString().trim().trimEnd('/')
            val phone = editPhone.text.toString().trim()
            val password = editPassword.text.toString()

            textError.text = ""

            if (siteUrl.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                textError.text = "همه فیلدها را پر کنید."
                return@setOnClickListener
            }
            if (!siteUrl.startsWith("http://") && !siteUrl.startsWith("https://")) {
                textError.text = "آدرس سایت باید با http:// یا https:// شروع شود."
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                val result = ApiClient.login(siteUrl, phone, password)
                progress.visibility = View.GONE
                btnLogin.isEnabled = true

                if (result.success && result.json != null) {
                    val token = result.json.optString("token")
                    Prefs.saveSession(this@LoginActivity, siteUrl, token, phone)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    textError.text = result.errorMessage ?: "ورود ناموفق بود."
                }
            }
        }
    }
}
