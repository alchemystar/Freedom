package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.engine.net.proto.mysql.OkPacket;
import alchemystar.engine.net.proto.util.ErrorCode;
import alchemystar.engine.net.proto.util.StringUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * KillHandler
 *
 * @Author lizhuyang
 */
public class KillHandler {

    public static void handle(String stmt, int offset, FrontendConnection c) {
        ChannelHandlerContext ctx = c.getCtx();
        String id = stmt.substring(offset).trim();
        if (StringUtil.isEmpty(id)) {
            c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "NULL connection id");
        } else {
            // get value
            long value = 0;
            try {
                value = Long.parseLong(id);
            } catch (NumberFormatException e) {
                c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "Invalid connection id:" + id);
                return;
            }

            // kill myself
            if (value == c.getId()) {
                getOkPacket().write(ctx);
                return;
            }

            // get connection and close it
            FrontendConnection fc = FrontendGroupHandler.frontendGroup.get(value);

            if (fc != null) {
                fc.close();
                getOkPacket().write(ctx);
            } else {
                c.writeErrMessage(ErrorCode.ER_NO_SUCH_THREAD, "Unknown connection id:" + id);
            }
        }
    }

    private static OkPacket getOkPacket() {
        OkPacket packet = new OkPacket();
        packet.packetId = 1;
        packet.affectedRows = 0;
        packet.serverStatus = 2;
        return packet;
    }

}