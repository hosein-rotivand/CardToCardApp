package com.example.cardtocard

import android.content.Context
import android.content.SharedPreferences

/**
 * ذخیره‌سازی ساده تنظیمات (آدرس سایت، توکن، شماره).
 * برای نسخه اولیه از SharedPreferences معمولی استفاده شده؛
 * در نسخه نهایی پیشنهاد می‌شود از EncryptedSharedPreferences استفاده شود.
 */
object Prefs {
    private const val PREFS_NAME = "cctc_prefs"
    private const val KEY_SITE_URL = "site_url"
    private const val KEY_TOKEN = "token"
    private const val KEY_PHONE = "phone"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(context: Context, siteUrl: String, token: String, phone: String) {
        prefs(context).edit()
            .putString(KEY_SITE_URL, siteUrl.trimEnd('/'))
            .putString(KEY_TOKEN, token)
            .putString(KEY_PHONE, phone)
            .apply()
    }

    fun getSiteUrl(context: Context): String? = prefs(context).getString(KEY_SITE_URL, null)
    fun getToken(context: Context): String? = prefs(context).getString(KEY_TOKEN, null)
    fun getPhone(context: Context): String? = prefs(context).getString(KEY_PHONE, null)

    fun isLoggedIn(context: Context): Boolean =
        !getSiteUrl(context).isNullOrEmpty() && !getToken(context).isNullOrEmpty()

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun saveLastSmsStatus(context: Context, status: String) {
        prefs(context).edit().putString("last_sms_status", status).apply()
    }

    fun getLastSmsStatus(context: Context): String =
        prefs(context).getString("last_sms_status", "-") ?: "-"
}
