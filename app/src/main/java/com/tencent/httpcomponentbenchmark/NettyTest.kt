package com.tencent.httpcomponentbenchmark

import android.util.Log
import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.net.URI
import java.net.URISyntaxException
import javax.net.ssl.SSLException
import io.netty.handler.codec.http.HttpContentDecompressor
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.LastHttpContent
import io.netty.util.CharsetUtil
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpObject
import io.netty.channel.SimpleChannelInboundHandler


/*
 * Author: wiizhang@tencent.com
 * Time: 2019/1/18
 */

class NettyTest : AbsTestBase() {

    companion object {
        private const val TAG = "NettyTest"
    }

    init {

    }

    override fun beforeConnect(url: String) {
        Log.i(TAG, "beforeConnect() : $url")
    }

    override fun afterConnect(url: String) {
        Log.d(TAG, "afterConnect() : $url")
    }

    override fun errorHit(t: Throwable) {
        Log.w(TAG, "errorHit() : ${t.localizedMessage}")
    }

    override fun doConnect(url: String, headers: Map<String, String>?) {
        try {
            val uri = URI(url)
            val scheme = uri.scheme ?: "http"
            val host = uri.host
            var port = uri.port
            val ssl = "https".equals(scheme, ignoreCase = true)

            if (port == -1) {
                if ("http".equals(scheme, ignoreCase = true)) {
                    port = 80
                } else if (ssl) {
                    port = 443
                }
            }
            if (!"http".equals(scheme, ignoreCase = true) && !ssl) {
                Log.e(TAG, "Only HTTP(S) is supported.")
                return
            }

            val sslCtx = if (ssl) {
                SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build()
            } else {
                null
            }

            val group = NioEventLoopGroup()
            try {
                val bootstrap = Bootstrap()
                val initializer = HttpClientInitializer(sslCtx)
                bootstrap.group(group).channel(NioSocketChannel::class.java)
                        .handler(initializer)

                saveTimeStamp(initializer)
                // Make the connection attempt.
                val channel = bootstrap.connect(host, port).sync().channel()

                // Prepare the HTTP request.
                val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url)
                request.headers().set(HttpHeaderNames.HOST, host)
                request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.KEEP_ALIVE)
                request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP)

                // Send the HTTP request.
                channel.writeAndFlush(request)

                // Wait for the server to close the connection.
                channel.closeFuture().sync()
            } finally {
                group.shutdownGracefully()
            }
        } catch (e: URISyntaxException) {
            Log.w(TAG, "doConnect() fail: ${e.localizedMessage}")
        } catch (e: SSLException) {
            Log.w(TAG, "doConnect() fail: ${e.localizedMessage}")
        } catch (e: InterruptedException) {
            Log.w(TAG, "doConnect() fail: ${e.localizedMessage}")
        }
    }

    inner class HttpClientInitializer(private val sslCtx: SslContext?) : ChannelInitializer<SocketChannel>() {

        public override fun initChannel(ch: SocketChannel) {
            val p = ch.pipeline()

            // Enable HTTPS if necessary.
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc()))
            }

            p.addLast(HttpClientCodec())

            // Remove the following line if you don't want automatic content decompression.
            p.addLast(HttpContentDecompressor())

            // Uncomment the following line if you don't want to handle HttpContents.
            //p.addLast(new HttpObjectAggregator(1048576));

            p.addLast(HttpClientHandler(this@HttpClientInitializer))
        }
    }

    inner class HttpClientHandler(private val initializer: HttpClientInitializer) : SimpleChannelInboundHandler<HttpObject>() {

        val strBuf = StringBuffer()

        public override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
            if (msg is HttpResponse) {
                Log.i(TAG, "STATUS: ${msg.status()}")
                Log.i(TAG, "VERSION: ${msg.protocolVersion()}")

                if (!msg.headers().isEmpty) {
                    for (name in msg.headers().names()) {
                        for (value in msg.headers().getAll(name)) {
                            Log.i(TAG, "HEADER: $name = $value")
                        }
                    }
                }
            }
            if (msg is HttpContent) {
                strBuf.append(msg.content().toString(CharsetUtil.UTF_8))

                if (msg is LastHttpContent) {
                    ctx.close()
                    Log.i(TAG, "Response: $strBuf")
                    Log.i(TAG, "TimeConsume: ${getTimeConsume(initializer)}")
                }
            }
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            errorHit(cause)
            ctx.close()
        }
    }
}

