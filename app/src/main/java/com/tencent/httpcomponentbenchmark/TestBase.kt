package com.tencent.httpcomponentbenchmark

import java.util.concurrent.ConcurrentHashMap

/*
 * Author: wiizhang@tencent.com
 * Time: 2019/1/18
 */
 
interface ITestBase {

    fun connect(url: String, headers: Map<String, String>? = null)

    fun beforeConnect(url: String)

    fun afterConnect(url: String)

    fun errorHit(t: Throwable)

    fun getErrors(): Int
}


abstract class AbsTestBase: ITestBase {

    private var errors: Int = 0
    private var stampMap = ConcurrentHashMap<Any, Long>()

    protected fun getTimeConsume(key: Any): Long {
        stampMap[key]?.let {
            return System.currentTimeMillis() - it
        }
        return -1
    }

    protected fun saveTimeStamp(key: Any) {
        stampMap[key] = System.currentTimeMillis()
    }

    override fun beforeConnect(url: String) {
    }

    override fun afterConnect(url: String) {
    }

    final override fun connect(url: String, headers: Map<String, String>?) {
        beforeConnect(url)
        try {
            doConnect(url, headers)
        } catch(e: Exception) {
            errorHit(e)
        }
        afterConnect(url)
    }

    protected abstract fun doConnect(url: String, headers: Map<String, String>? = null)

    final override fun getErrors() = errors

}