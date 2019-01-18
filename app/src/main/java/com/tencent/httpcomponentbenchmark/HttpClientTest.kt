package com.tencent.httpcomponentbenchmark

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.HandlerDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import java.io.BufferedInputStream
import org.apache.http.message.BasicHeader



/*
 * Author: wiizhang@tencent.com
 * Time: 2019/1/18
 */

class HttpClientTest : AbsTestBase() {

    companion object {
        private const val TAG = "HttpClientTest"
    }

    private val client: HttpClient
    private val thread = HandlerThread("HttpClientTest")
    private val dispatcher: HandlerDispatcher

    init {
        client = DefaultHttpClient()
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
        GlobalScope.launch(context = dispatcher) {
            val request = HttpGet(url)
            request.setHeader(BasicHeader("Pragma", "no-cache"))
            request.setHeader(BasicHeader("Cache-Control", "no-cache, no-store, must-revalidate, post-check=0, pre-check=0"))
            saveTimeStamp(request)
            //第三步:执行请求。使用HttpClient的execute方法，执行刚才构建的请求
            val response = client.execute(request)
            //判断请求是否成功
            if (response.statusLine.statusCode == HttpStatus.SC_OK) {
                Log.i(TAG, "on response")
                val inStream = BufferedInputStream(response.entity.content)
                inStream.use { inStream ->
                    val buffer = if (response.entity.contentLength > 0) {
                        ByteArray(response.entity.contentLength.toInt())
                    } else {
                        Log.w(TAG, "on content-length provided")
                        ByteArray(8192)
                    }

                    var readCount = inStream.read(buffer)
                    var content = String(buffer, 0, readCount)
                    Log.i(TAG, "response: \r\n $content")
                    if (response.entity.isChunked) {
                        while(readCount != -1) {
                            readCount = inStream.read(buffer)
                            if (readCount == -1) {
                                break
                            }
                            content = String(buffer, 0, readCount)
                            Log.i(TAG, content)
                        }
                    }
                }
                Log.i(TAG, "TimeConsume ${getTimeConsume(request)}")
            } else {
                errorHit(RuntimeException("Request failed, status = ${response.statusLine.statusCode}"))
            }
        }
    }
}