package com.example.cardtocard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * دریافت پیامک ورودی و ارسال متن آن به وبهوک وردپرس.
 * توجه: این پیامک از تمام شماره‌ها دریافت و ارسال می‌شود (فیلتر بانک در سمت
 * سرور انجام می‌شود). برای فیلتر کردن شماره فرستنده در سمت اپ، می‌توان در
 * نسخه‌های بعدی یک لیست شماره مجاز به تنظیمات اضافه کرد.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        if (!Prefs.isLoggedIn(context)) {
            return // اپ هنوز لاگین نشده، کاری انجام نمی‌شود
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) {
            return
        }

        val sender = messages[0].originatingAddress ?: ""
        val fullText = messages.joinToString(separator = "") { it.messageBody ?: "" }

        val siteUrl = Prefs.getSiteUrl(context) ?: return
        val token = Prefs.getToken(context) ?: return

        // چون onReceive باید سریع تمام شود، از goAsync برای اجازه کار async استفاده می‌کنیم
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = ApiClient.sendSms(siteUrl, token, fullText, sender)
                val status = if (result.success) {
                    val matched = result.json?.optBoolean("matched", false) ?: false
                    if (matched) "✓ تطبیق داده شد و سفارش تایید شد" else "ارسال شد، تطبیقی پیدا نشد"
                } else {
                    "خطا: ${result.errorMessage}"
                }
                Prefs.saveLastSmsStatus(context, status)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
