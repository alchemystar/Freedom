package alchemystar.freedom.engine.net.handler.factory;

import alchemystar.engine.config.SystemConfig;
import alchemystar.engine.net.codec.MySqlPacketDecoder;
import alchemystar.engine.net.handler.frontend.FrontendAuthenticator;
import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.handler.frontend.FrontendGroupHandler;
import alchemystar.engine.net.handler.frontend.FrontendTailHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 前端handler工厂
 *
 * @Author lizhuyang
 */
public class FrontHandlerFactory extends ChannelInitializer<SocketChannel> {

    private FrontConnectionFactory factory;

    public FrontHandlerFactory() {
        factory = new FrontConnectionFactory();
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        FrontendConnection source = factory.getConnection();
        FrontendGroupHandler groupHandler = new FrontendGroupHandler(source);
        FrontendAuthenticator authHandler = new FrontendAuthenticator(source);
        FrontendTailHandler tailHandler = new FrontendTailHandler(source);
        // 心跳handler
        ch.pipeline().addLast(new IdleStateHandler(SystemConfig.IDLE_CHECK_INTERVAL, SystemConfig.IDLE_CHECK_INTERVAL, SystemConfig.IDLE_CHECK_INTERVAL));
        // decode mysql packet depend on it's length
        ch.pipeline().addLast(new MySqlPacketDecoder());
        ch.pipeline().addLast(groupHandler);
        ch.pipeline().addLast(authHandler);
        ch.pipeline().addLast(tailHandler);

    }
}