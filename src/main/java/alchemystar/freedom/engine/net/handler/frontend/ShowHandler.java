package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.freedom.engine.net.response.ErrResponse;
import alchemystar.freedom.engine.net.response.ShowDatabases;
import alchemystar.freedom.engine.net.response.ShowTables;
import alchemystar.freedom.engine.parser.ServerParseShow;

/**
 * ShowHandler
 *
 * @Author lizhuyang
 */
public final class ShowHandler {

    public static void handle(String stmt, FrontendConnection c, int offset) {
        switch (ServerParseShow.parse(stmt, offset)) {
            case ServerParseShow.DATABASES:
                ShowDatabases.response(c);
                break;
            case ServerParseShow.SHOWTABLES:
                ShowTables.response(c);
                break;
            default:
                ErrResponse.response(c, "not support this set param");
                break;
        }
    }
}