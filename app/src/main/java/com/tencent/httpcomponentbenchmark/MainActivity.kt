package com.tencent.httpcomponentbenchmark

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TEST_URL1 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1547809902281&di=e95bbe43875e038df0176f992ccd3e25&imgtype=0&src=http%3A%2F%2Fvpic.video.qq.com%2F39649808%2Fk0165g5o94a_ori_3.jpg"
        private const val TEST_URL2 = "http://article-fd.zol-img.com.cn/t_s1280x720/g5/M00/03/08/ChMkJ1w_-SiIR-chAAMs3-3Jj1IAAucCwPFOykAAyz3690.jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        plainUseCase(NettyTest())
        plainUseCase(OkHttpTest())
        plainUseCase(HttpClientTest())
    }

    private fun plainUseCase(instance: ITestBase) {
        Log.d("TimeConsume", "======> first")
        repeat(1) { instance.connect(TEST_URL1) }
        Thread.sleep(1000L)
        Log.d("TimeConsume", "======> second")
        repeat(1) { instance.connect(TEST_URL2) }
        Thread.sleep(1000L)
        Log.w("TimeConsume", "-------------------------------")
    }
}
