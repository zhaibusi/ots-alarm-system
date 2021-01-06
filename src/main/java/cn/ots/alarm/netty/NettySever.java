package cn.ots.alarm.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * netty服务
 *
 * @author
 * @since 2020/12/5 12:07
 */
@Component
public class NettySever
{
    public static final Logger LOGGER = LoggerFactory.getLogger(NettySever.class);

    /**
     * netty对外服务端口号
     */
    @Value("${netty.server.port}")
    public int port;

    public void init()
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                protected void initChannel(SocketChannel socketChannel)
                {
                    socketChannel.pipeline()
                        .addLast(new ByteArrayDecoder())
                        .addLast(new ByteArrayEncoder())
                        .addLast(new NettyServerHandler());
                }
            });
            LOGGER.info("NettySever-init-success");
            Channel ch = b.bind(port).sync().channel();
            ch.closeFuture().sync();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            //优雅的退出程序
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }
}
