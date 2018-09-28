package alchemystar.freedom.engine.net.handler.factory;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.engine.net.handler.frontend.FrontendConnection;
import alchemystar.freedom.engine.net.handler.frontend.ServerQueryHandler;

/**
 * FrontendConnection 工厂类
 *
 * @Author lizhuyang
 */
public class FrontConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(FrontConnectionFactory.class);

    /**
     * MySql ThreadId Generator
     */
    private static final AtomicInteger ACCEPT_SEQ = new AtomicInteger(0);

    public FrontendConnection getConnection() {
        FrontendConnection connection = new FrontendConnection();
        connection.setQueryHandler(new ServerQueryHandler(connection));
        connection.setId(ACCEPT_SEQ.getAndIncrement());
        logger.info("connection Id=" + connection.getId());
        connection.setCharset(SystemConfig.DEFAULT_CHARSET);
        connection.setTxIsolation(SystemConfig.DEFAULT_TX_ISOLATION);
        connection.setLastActiveTime();
        return connection;
    }
}
