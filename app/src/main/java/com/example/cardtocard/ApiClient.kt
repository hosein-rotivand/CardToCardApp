package com.example.cardtocard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * کلاینت ساده HTTP برای ارتباط با REST API افزونه وردپرس.
 * از HttpURLConnection استفاده شده تا به کتابخانه اضافه (مثل Retrofit) نیازی نباشد.
 */
object ApiClient {

    data class ApiResult(val success: Boolean, val json: JSONObject?, val errorMessage: String?)

    suspend fun login(siteUrl: String, phone: String, password: String): ApiResult =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$siteUrl/wp-json/cctc/v1/login")
                val conn = openConnection(url, "POST")

                val body = JSONObject()
                body.put("phone", phone)
                body.put("password", password)

                writeBody(conn, body.toString())

                val code = conn.responseCode
                val responseText = readResponse(conn)
                val json = JSONObject(responseText)

                if (code in 200..299 && json.optBoolean("success", false)) {
                    ApiResult(true, json, null)
                } else {
                    ApiResult(false, json, json.optString("message", "خطای ورود"))
                }
            } catch (e: Exception) {
                ApiResult(false, null, "خطا در اتصال: ${e.message}")
            }
        }

    suspend fun sendSms(siteUrl: String, token: String, smsText: String, sender: String?): ApiResult =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$siteUrl/wp-json/cctc/v1/sms")
                val conn = openConnection(url, "POST")
                conn.setRequestProperty("Authorization", "Bearer $token")

                val body = JSONObject()
                body.put("text", smsText)
                body.put("sender", sender ?: "")

                writeBody(conn, body.toString())

                val code = conn.responseCode
                val responseText = readResponse(conn)
                val json = JSONObject(responseText)

                ApiResult(code in 200..299, json, json.optString("message", null))
            } catch (e: Exception) {
                ApiResult(false, null, "خطا در ارسال: ${e.message}")
            }
        }

    private fun openConnection(url: URL, method: String): HttpURLConnection {
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        if (conn is HttpsURLConnection) {
            // اتصال HTTPS استاندارد؛ در صورت استفاده از دامنه با گواهی معتبر نیازی به تنظیم خاص نیست
        }
        return conn
    }

    private fun writeBody(conn: HttpURLConnection, body: String) {
        conn.outputStream.use { os ->
            os.write(body.toByteArray(Charsets.UTF_8))
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: "{}"
    }
}
