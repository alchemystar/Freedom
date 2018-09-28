package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.engine.net.response.ErrResponse;
import alchemystar.engine.net.response.ShowCreateTable;
import alchemystar.engine.net.response.ShowDatabases;
import alchemystar.engine.net.response.ShowTables;
import alchemystar.engine.net.response.jdbc.JdbcVariableResponse;
import alchemystar.engine.net.response.jdbc.ShowCollationResponse;
import alchemystar.engine.net.response.jdbc.ShowFullColumnsResponse;
import alchemystar.engine.net.response.jdbc.ShowFullTablesResponse;
import alchemystar.engine.net.response.jdbc.ShowKeysResponse;
import alchemystar.engine.parser.ServerParseShow;

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
            case ServerParseShow.FULL_TABLES:
                ShowFullTablesResponse.response(c, stmt);
                break;
            case ServerParseShow.FULL_COLUMNS:
                ShowFullColumnsResponse.response(c, stmt);
                break;
            case ServerParseShow.COLLATION:
                ShowCollationResponse.response(c);
                break;
            case ServerParseShow.KEYS:
                ShowKeysResponse.response(c, stmt);
                break;
            case ServerParseShow.VARIABLES:
                JdbcVariableResponse.response(c);
                break;
            case ServerParseShow.SHOWTABLES:
                ShowTables.response(c);
                break;
            case ServerParseShow.SHOW_CREATE_TABLE:
                ShowCreateTable.response(c, stmt);
                break;
            default:
                ErrResponse.response(c, "not support this set param");
                break;
        }
    }
}