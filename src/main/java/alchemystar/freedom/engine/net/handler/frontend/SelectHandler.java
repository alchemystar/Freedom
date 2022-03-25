package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.freedom.engine.net.response.SelectDatabase;
import alchemystar.freedom.engine.net.response.SelectVersion;
import alchemystar.freedom.engine.net.response.SelectVersionComment;
import alchemystar.freedom.engine.net.response.jdbc.SelectIncrementResponse;
import alchemystar.freedom.engine.parser.ServerParseCheck;
import alchemystar.freedom.engine.parser.ServerParseSelect;

/**
 * SelectHandler
 *
 * @Author lizhuyang
 */
public final class SelectHandler {

    private static String selectIncrement = "SELECT @@session.auto_increment_increment";

    public static void handle(String stmt, FrontendConnection c, int offs) {
        int offset = offs;
        switch (ServerParseSelect.parse(stmt, offs)) {
            case ServerParseSelect.DATABASE:
                SelectDatabase.response(c);
                break;
            case ServerParseSelect.VERSION_COMMENT:
                SelectVersionComment.response(c);
                break;
            case ServerParseSelect.VERSION:
                SelectVersion.response(c);
                break;
            default:
                if (selectIncrement.equals(stmt)) {
                    SelectIncrementResponse.response(c);
                } else {
                    c.execute(stmt, ServerParseCheck.SELECT);
                }
                break;
        }
    }

}