package alchemystar.freedom.engine.net.response;

import org.apache.commons.lang.StringUtils;

import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.ErrorPacket;

/**
 * ErrResponse
 *
 * @Author lizhuyang
 */
public class ErrResponse {

    public static void response(FrontendConnection connection, String errMsg) {
        if (StringUtils.isNotEmpty(errMsg)) {
            ErrorPacket errorPacket = new ErrorPacket();
            errorPacket.message = errMsg.getBytes();
            errorPacket.write(connection.getCtx());
        }
    }
}
