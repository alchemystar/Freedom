package alchemystar.freedom.engine.net.handler.frontend;

import alchemystar.freedom.engine.parser.ServerParseCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.freedom.engine.net.proto.util.ErrorCode;
import alchemystar.freedom.engine.parser.ServerParse;

/**
 * ServerQueryHandler
 *
 * @Author lizhuyang
 */
public class ServerQueryHandler implements FrontendQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger("sql-digest");

    private FrontendConnection source;

    public ServerQueryHandler(FrontendConnection source) {
        this.source = source;
    }

    public void query(String origin) {

        logger.info("sql = " + origin);
        String sql = removeFirstAnnotation(origin);
        int rs = ServerParse.parse(sql);
        switch (rs & 0xff) {
            case ServerParseCheck.SET:
                SetHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParseCheck.SHOW:
                ShowHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParseCheck.SELECT:
                SelectHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParseCheck.START:
                StartHandler.handle(sql, source, rs >>> 8);
                break;
            case ServerParseCheck.BEGIN:
                source.begin();
                break;
            case ServerParseCheck.SAVEPOINT:
                SavepointHandler.handle(sql, source);
                break;
            case ServerParseCheck.KILL:
                KillHandler.handle(sql, rs >>> 8, source);
                break;
            case ServerParseCheck.KILL_QUERY:
                source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
                break;
            case ServerParseCheck.EXPLAIN:
                source.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
                break;
            case ServerParseCheck.CREATE_DATABASE:
                // source.createShema(sql);
                break;
            case ServerParseCheck.COMMIT:
                source.commit();
                break;
            case ServerParseCheck.ROLLBACK:
                source.rollBack();
                break;
            case ServerParseCheck.USE:
                UseHandler.handle(sql, source, rs >>> 8);
                break;
            default:
                // todo add no modify exception
                source.execute(sql, rs);
        }
    }

    public static String removeFirstAnnotation(String sql) {
        String result = null;
        sql = sql.trim();
        if (sql.startsWith("/*")) {
            int index = sql.indexOf("*/") + 2;
            return sql.substring(index);
        } else {
            return sql;
        }
    }
}
