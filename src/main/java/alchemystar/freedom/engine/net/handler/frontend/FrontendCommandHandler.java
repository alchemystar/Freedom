package alchemystar.freedom.engine.net.handler.frontend;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.engine.net.proto.MySQLPacket;
import alchemystar.freedom.engine.net.proto.mysql.BinaryPacket;
import alchemystar.freedom.engine.net.proto.util.ErrorCode;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 命令Handler
 *
 * @Author lizhuyang
 */
public class FrontendCommandHandler extends ChannelHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ChannelHandlerAdapter.class);

    protected FrontendConnection source;

    public FrontendCommandHandler(FrontendConnection source) {
        this.source = source;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 重置最后active时间
        source.setLastActiveTime();
        BinaryPacket bin = (BinaryPacket) msg;
        byte type = bin.data[0];
        switch (type) {
            case MySQLPacket.COM_INIT_DB:
                // just init the frontend
                source.initDB(bin);
                break;
            case MySQLPacket.COM_QUERY:
                source.query(bin);
                break;
            case MySQLPacket.COM_PING:
                // todo ping , last access time update
                source.ping();
                break;
            case MySQLPacket.COM_QUIT:
                source.close();
                break;
            case MySQLPacket.COM_PROCESS_KILL:
                source.kill(bin.data);
                break;
            case MySQLPacket.COM_STMT_PREPARE:
                // todo prepare支持,参考MyCat
                source.stmtPrepare(bin.data);
                break;
            case MySQLPacket.COM_STMT_EXECUTE:
                source.stmtExecute(bin.data);
                break;
            case MySQLPacket.COM_STMT_CLOSE:
                source.stmtClose(bin.data);
                break;
            case MySQLPacket.COM_HEARTBEAT:
                source.heartbeat(bin.data);
                break;
            default:
                source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
                break;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 如果心跳检查>最大值,则close掉此连接
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state().equals(IdleState.ALL_IDLE)) {
                Long now = (new Date()).getTime();
                System.out.println("hahaha");
                if (now - source.getLastActiveTime() > (SystemConfig.IDLE_TIME_OUT * 1000)) {
                    source.close();
                }
            }
        }
    }

}
