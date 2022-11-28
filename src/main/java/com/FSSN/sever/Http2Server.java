//package com.FSSN.sever;
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.codec.http2.Http2SecurityUtil;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import io.netty.handler.ssl.*;
//import io.netty.handler.ssl.util.SelfSignedCertificate;
//
//import static io.netty.handler.ssl.ApplicationProtocolConfig.*;
//
//public final class Http2Server {
//    static final int PORT = 8443;
//
//    public static void main(String[] args) throws Exception {
//        SelfSignedCertificate ssc = new SelfSignedCertificate();
//        SslContext sslCtx =  SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
//                .sslProvider(SslProvider.JDK)
//                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
//                .applicationProtocolConfig(
//                        new ApplicationProtocolConfig(Protocol.ALPN, SelectorFailureBehavior.NO_ADVERTISE,
//                                SelectedListenerFailureBehavior.ACCEPT, ApplicationProtocolNames.HTTP_2))
//                .build();
//                NioEventLoopGroup group = new NioEventLoopGroup();
//        try {
//            ServerBootstrap b = new ServerBootstrap();
//            b.option(ChannelOption.SO_BACKLOG, 1024);
//            b.group(group)
//                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
//                    .childHandler(new ChannelInitializer() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) throws Exception {
//                            if (sslCtx != null) {
//                                ch.pipeline()
//                                        .addLast(sslCtx.newHandler(ch.alloc()), Http2UZztil.getServerAPNHandler());
//                            }
//                        }
//                    });
//            Channel ch = b.bind(PORT).sync().channel();
//
//            logger.info("HTTP/2 Server is listening on https://127.0.0.1:" + PORT + '/');
//
//            ch.closeFuture().sync();
//        } finally {
//            group.shutdownGracefully();
//        }
//    }
//}
