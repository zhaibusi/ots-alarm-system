package cn.ots.alarm.netty;

import cn.ots.alarm.constants.Constants;
import cn.ots.alarm.utils.ApplicationContextUtils;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * netty服务处理器
 *
 * @author
 * @since 2020/12/4 20:16
 */
@Component
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    public static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);

    private static NettyServiceHelper nettyServiceHelper;

    /**
     * 每当从服务端收到新的客户端连接时， 客户端的 Channel 存入ChannelGroup列表中，
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        NettySocketGroup.ALL_CHANNEL_MAP.put(ctx.channel().id().asLongText(), ctx.channel());
    }

    /**
     * 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        String cId = ctx.channel().id().asLongText();
        nettyServiceHelper.socketExit(cId);

    }

    /**
     * socket连接上服务端时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        LOGGER.info("[NettyServerHandler-client-online]address:{}" + incoming.remoteAddress());
    }

    /**
     * socket断开服务端时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel incoming = ctx.channel();
        LOGGER.info("[NettyServerHandler-client-offline]address:{}" + incoming.remoteAddress());
    }

    /**
     * 服务端接收客户端发送过来的数据结束之后调用
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
     * 在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉。
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        System.out.println("Client:" + incoming.remoteAddress() + "异常");
        LOGGER.error("[NettyServerHandler-exceptionCaught]address:{},e:{}" + incoming.remoteAddress(), cause);
        ctx.close();
    }

    /**
     * 服务端处理客户端webSocket请求的核心方法。
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Channel incoming = ctx.channel();
            String cId = ctx.channel().id().asLongText();
            if (Objects.isNull(msg)) {
                LOGGER.info("[NettyServerHandler-channelRead-obj-null]address:{},req:{},cId:{}", incoming.remoteAddress(), JSON.toJSON(msg),
                        cId);
                return;
            }
            if (msg instanceof byte[]) {
                byte[] msgbytes = (byte[]) msg;
                if (ArrayUtils.getLength(msgbytes) < Constants.MIN_OTS_REQ_ARR_LENGTH) {
                    LOGGER.info("[NettyServerHandler-channelRead-req-null]address:{},cId:{},msgbytes:{}", incoming.remoteAddress(), cId,
                            JSON.toJSON(msg));
                    return;
                }
                //请求指令
                byte req = msgbytes[0];
                //必须是02
                byte type = msgbytes[1];
                if (type != Constants.DEFAULT_REQ_TYPE) {
                    LOGGER.warn("[NettyServerHandler-channelRead-channel-type-illegal]address:{},req:{},type:{}" + incoming.remoteAddress(),
                            req, type);
                    return;
                }
                LOGGER.info("[NettyServerHandler-channelRead]address:{},req:{},cId:{},type:{}" + incoming.remoteAddress(), req, cId, type);
                //ots-主业务处理
                switch (req) {
                    case Constants.OTS_HANDSHAKE_BEGIN:
                        //3.1	握手命令（OTS->子系统）
                        //移出断开握手池
                        NettySocketGroup.HAND_BROKEN_CHANNEL_MAP.remove(cId);
                        //加入建立握手池
                        NettySocketGroup.HAND_CONNECT_CHANNEL_MAP.put(cId, ctx.channel());
                        getNettyServiceHandler().handShakeBeginHandler(incoming);
                        break;
                    case Constants.OTS_INQUIRY_ALARM_BEGIN:
                        //3.3	询问告警（OTS -> 子系统）
                        getNettyServiceHandler().inquiryAlarmBeginHandler(incoming);
                        break;
                    case Constants.OTS_FAILURE_ACK:
                        //3.6	故障确认（OTS ->子系统）
                        getNettyServiceHandler().failureAckHandler(incoming);
                        break;
                    case Constants.OTS_HANDSHAKE_END:
                        //3.7	握手断开（OTS -> 子系统）
                        //加入断开握手池
                        NettySocketGroup.HAND_BROKEN_CHANNEL_MAP.put(cId, ctx.channel());
                        //移除握手池子
                        NettySocketGroup.HAND_CONNECT_CHANNEL_MAP.remove(cId);
                        getNettyServiceHandler().handShakeEndHandler(incoming);
                        break;
                    default:
                        LOGGER.warn("[NettyServerHandler-channelRead-req-illegal]address:{},req:{},cId:{}" + incoming.remoteAddress(), req,
                                cId);

                }
            }
        } catch (Exception e) {
            LOGGER.warn("[NettyServerHandler-channelRead-error]e:{}", e);
        }
    }

    /**
     * 单例模式设置nettyServiceHelper
     *
     * @return
     */
    private NettyServiceHelper getNettyServiceHandler() {
        if (nettyServiceHelper == null) {
            synchronized (NettyServerHandler.class) {
                if (nettyServiceHelper == null) {
                    nettyServiceHelper = ApplicationContextUtils.getApplicationContext().getBean("nettyServiceHelper",
                            NettyServiceHelper.class);
                }
            }
        }
        return nettyServiceHelper;
    }

}
