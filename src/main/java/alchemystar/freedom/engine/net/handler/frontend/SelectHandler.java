package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.engine.net.response.SelectAutoIncrementResponse;
import alchemystar.engine.net.response.SelectDatabase;
import alchemystar.engine.net.response.SelectIdentity;
import alchemystar.engine.net.response.SelectLastInsertId;
import alchemystar.engine.net.response.SelectTxResponse;
import alchemystar.engine.net.response.SelectUser;
import alchemystar.engine.net.response.SelectVersion;
import alchemystar.engine.net.response.SelectVersionComment;
import alchemystar.engine.net.response.jdbc.SelectIncrementResponse;
import alchemystar.engine.parser.ServerParse;
import alchemystar.engine.parser.ServerParseSelect;
import alchemystar.engine.parser.util.ParseUtil;

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
            case ServerParseSelect.VERSION_COMMENT:
                SelectVersionComment.response(c);
                break;
            case ServerParseSelect.DATABASE:
                SelectDatabase.response(c);
                break;
            case ServerParseSelect.USER:
                SelectUser.response(c);
                break;
            case ServerParseSelect.VERSION:
                SelectVersion.response(c);
                break;
            case ServerParseSelect.LAST_INSERT_ID:
                offset = ParseUtil.move(stmt, 0, "select".length());
                loop:
                for (; offset < stmt.length(); ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                            continue;
                        case '/':
                        case '#':
                            offset = ParseUtil.comment(stmt, offset);
                            continue;
                        case 'L':
                        case 'l':
                            break loop;
                    }
                }
                offset = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, offset);
                offset = ServerParseSelect.skipAs(stmt, offset);
                SelectLastInsertId.response(c, stmt, offset);
                break;
            case ServerParseSelect.TX_ISOLATION:
                SelectTxResponse.response(c);
                break;
            case ServerParseSelect.AUTO_INCREMENT:
                SelectAutoIncrementResponse.response(c);
                break;
            case ServerParseSelect.IDENTITY:
                offset = ParseUtil.move(stmt, 0, "select".length());
                loop:
                for (; offset < stmt.length(); ++offset) {
                    switch (stmt.charAt(offset)) {
                        case ' ':
                            continue;
                        case '/':
                        case '#':
                            offset = ParseUtil.comment(stmt, offset);
                            continue;
                        case '@':
                            break loop;
                    }
                }
                int indexOfAtAt = offset;
                offset += 2;
                offset = ServerParseSelect.indexAfterIdentity(stmt, offset);
                String orgName = stmt.substring(indexOfAtAt, offset);
                offset = ServerParseSelect.skipAs(stmt, offset);
                SelectIdentity.response(c, stmt, offset, orgName);
                break;
            default:
                if (selectIncrement.equals(stmt)) {
                    SelectIncrementResponse.response(c);
                } else {
                    c.execute(stmt, ServerParse.SELECT);
                }
                break;
        }
    }

}