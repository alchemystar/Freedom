package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.engine.net.proto.util.ErrorCode;

/**
 * BeginHandler
 *
 * @Author lizhuyang
 */
public final class BeginHandler {

    public static void handle(String stmt, FrontendConnection c) {
        c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
    }

}