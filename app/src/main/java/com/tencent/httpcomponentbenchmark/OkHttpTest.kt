package com.tencent.httpcomponentbenchmark

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.HandlerDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/*
 * Author: wiizhang@tencent.com
 * Time: 2019/1/18
 */
 
class OkHttpTest: AbsTestBase() {

    companion object {
        private const val TAG = "OkHttpTest"
    }

    private val client: OkHttpClient
    private val thread = HandlerThread("HttpClientTest")
    private val dispatcher: HandlerDispatcher
    private val trustManager: X509TrustManager

    init {
        trustManager = object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        val sslContext = SSLContext.getInstance("TLS")
        // 用上面得到的trustManagers初始化SSLContext，这样sslContext就会信任keyStore中的证书
        // 第一个参数是授权的密钥管理器，用来授权验证，比如授权自签名的证书验证。第二个是被授权的证书管理器，用来验证服务器端的证书
        sslContext.init(null, arrayOf(trustManager), null)
        client = OkHttpClient().newBuilder()
                .sslSocketFactory(sslContext.socketFactory, trustManager)
                .cache(null)
                .build()
        thread.start()
        dispatcher = Handler(thread.looper).asCoroutineDispatcher()
    }

    override fun beforeConnect(url: String) {
        Log.i(TAG, "beforeConnect() $url")
    }

    override fun afterConnect(url: String) {
        Log.i(TAG, "afterConnect() $url")
    }

    override fun errorHit(t: Throwable) {
        Log.w(TAG, "errorHit() ${t.localizedMessage}")
    }

    override fun doConnect(url: String, headers: Map<String, String>?) {
        GlobalScope.launch (context = dispatcher) {
            val request = Request.Builder().url(url)
                    .header("Pragma", "no-cache")
                    .header("Cache-Control", "no-cache")
                    .header("Expires", "0")
                    .build()
            saveTimeStamp(request)
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                errorHit(IOException("Unexpected code ${response.code()}"))
            }
            Log.i(TAG, "onResponse() ${request.url().url()}")

            val responseHeaders = response.headers()
            for (i in 0 until responseHeaders.size()) {
                Log.i(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i))
            }
            Log.i(TAG, "Response: \r\n")
            response.body()?.source()?.let {
                source ->
                val content = String(source.readByteArray())
                Log.i(TAG, content)
            }
            Log.i(TAG, "TimeConsume ${getTimeConsume(request)}")
        }
    }
}