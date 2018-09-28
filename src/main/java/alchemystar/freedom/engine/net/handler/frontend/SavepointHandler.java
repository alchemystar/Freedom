package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.freedom.engine.net.proto.util.ErrorCode;

/**
 * SavePointHandler
 *
 * @Author lizhuyang
 */
public final class SavepointHandler {

    public static void handle(String stmt, FrontendConnection c) {
        c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
    }

}
