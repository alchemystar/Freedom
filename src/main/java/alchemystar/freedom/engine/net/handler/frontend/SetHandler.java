package alchemystar.freedom.engine.net.handler.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.engine.Session;
import alchemystar.engine.net.proto.mysql.OkPacket;
import alchemystar.engine.net.proto.util.ErrorCode;
import alchemystar.engine.net.proto.util.Isolations;
import alchemystar.engine.net.response.CharacterSet;
import alchemystar.engine.parser.ServerParseSet;
import alchemystar.schema.Schema;
import alchemystar.table.Table;
import io.netty.channel.ChannelHandlerContext;

/**
 * SetHandler
 *
 * @Author lizhuyang
 */
public final class SetHandler {

    private static final Logger logger = LoggerFactory.getLogger(SetHandler.class);
    private static final byte[] AC_OFF = new byte[] {7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
    private static final String TABLE_PATH_SYNTAX_ERROR = "must set like this [table_path=\"tableName:filePath\"";

    public static void handle(String stmt, FrontendConnection c, int offset) {
        ChannelHandlerContext ctx = c.getCtx();
        int rs = ServerParseSet.parse(stmt, offset);
        switch (rs & 0xff) {
            case ServerParseSet.AUTOCOMMIT_ON:
                if (c.isAutocommit()) {
                    c.writeBuf(OkPacket.OK);
                } else {
                    //c.commit();
                    c.setAutocommit(true);
                }
                break;
            case ServerParseSet.AUTOCOMMIT_OFF: {
                if (c.isAutocommit()) {
                    c.setAutocommit(false);
                }
                c.writeOk();
                break;
            }
            case ServerParseSet.TX_READ_UNCOMMITTED: {
                c.setTxIsolation(Isolations.READ_UNCOMMITTED);
                c.writeOk();
                break;
            }
            case ServerParseSet.TX_READ_COMMITTED: {
                c.setTxIsolation(Isolations.READ_COMMITTED);
                c.writeOk();
                break;
            }
            case ServerParseSet.TX_REPEATED_READ: {
                c.setTxIsolation(Isolations.REPEATED_READ);
                c.writeOk();
                break;
            }
            case ServerParseSet.TX_SERIALIZABLE: {
                c.setTxIsolation(Isolations.SERIALIZABLE);
                c.writeOk();
                break;
            }
            case ServerParseSet.NAMES:
                String charset = stmt.substring(rs >>> 8).trim();
                if (c.setCharset(charset)) {
                    c.writeOk();
                } else {
                    c.writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
                }
                break;
            case ServerParseSet.CHARACTER_SET_CLIENT:
            case ServerParseSet.CHARACTER_SET_CONNECTION:
            case ServerParseSet.CHARACTER_SET_RESULTS:
                CharacterSet.response(stmt, c, rs);
                break;
            case ServerParseSet.TABLE_PATH:
                setPath(stmt, c.getSession());
                c.writeOk();
                break;
            default:
                StringBuilder s = new StringBuilder();
                logger.warn(s.append(c).append(stmt).append(" is not executed").toString());
                c.writeOk();
        }
    }

    public static void setPath(String stmt, Session session) {
        String stmtTrim = stmt.substring(4, stmt.length()).trim();
        String cutString = stmtTrim.substring(11, stmtTrim.length());
        if (!cutString.startsWith("\"") || !cutString.endsWith("\"")) {
            throw new RuntimeException("use [set table_path=\"tableName:tablePath\"");
        }
        cutString = cutString.substring(1, cutString.length() - 1);
        String[] args = cutString.split(":");
        if (args.length != 2) {
            throw new RuntimeException(TABLE_PATH_SYNTAX_ERROR);
        }
        String[] tableSplits = args[0].split("\\.");
        Table table = null;
        String filePath = args[1];
        if (tableSplits.length == 1) {
            table = session.getCurrentSchema().getTableOrView(tableSplits[0]);
            if (table == null) {
                throw new RuntimeException(
                        "Table:" + tableSplits[0] + " not find in schema:" + session.getCurrentSchema()
                                .getName());
            }
            session.loadTable(table.getName(), filePath);
        } else {
            String schemaName = tableSplits[0];
            String tableName = tableSplits[1];
            Schema schema = session.getDatabase().findSchema(schemaName);
            if (schema == null) {
                throw new RuntimeException("can't find schema:" + schemaName);
            }
            Table t = schema.getTableOrView(tableName);
            if (t == null) {
                throw new RuntimeException(
                        "Table:" + tableName + " not find in schema:" + schemaName);
            }
            session.loadTable(schemaName, tableName, filePath);
        }
    }

}