package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.freedom.engine.net.proto.util.ErrorCode;
import alchemystar.freedom.engine.parser.ServerParseCheck;
import alchemystar.freedom.engine.parser.ServerParseStart;

/**
 * StartHandler
 *
 * @Author lizhuyang
 */
public final class StartHandler {

    public static void handle(String stmt, FrontendConnection c, int offset) {
        switch (ServerParseStart.parse(stmt, offset)) {
            case ServerParseStart.TRANSACTION:
                c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported statement");
                break;
            default:
                // todo data source
                  c.execute(stmt, ServerParseCheck.START);
                break;
        }
    }

}