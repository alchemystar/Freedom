package alchemystar.freedom.engine.net.handler.frontend;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemystar.engine.Session;
import alchemystar.engine.net.proto.mysql.BinaryPacket;
import alchemystar.engine.net.proto.mysql.ErrorPacket;
import alchemystar.engine.net.proto.mysql.MySQLMessage;
import alchemystar.engine.net.proto.mysql.OkPacket;
import alchemystar.engine.net.proto.util.CharsetUtil;
import alchemystar.engine.net.proto.util.ErrorCode;
import alchemystar.engine.net.response.OkResponse;
import alchemystar.engine.net.response.SelectResponse;
import alchemystar.engine.parser.ServerParse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 前端连接
 *
 * @Author lizhuyang
 */
public class FrontendConnection {

    public static final int packetHeaderSize = 4;
    private static final Logger logger = LoggerFactory.getLogger(FrontendConnection.class);

    protected long id;
    protected String user;
    protected String host;
    protected int port;
    protected String schema;
    protected String charset;
    protected int charsetIndex;
    protected FrontendQueryHandler queryHandler;
    protected ChannelHandlerContext ctx;
    protected Session session;
    // update by the ResponseHandler
    private long lastInsertId;
    private long lastActiveTime;

    private static final long AUTH_TIMEOUT = 15 * 1000L;

    private volatile int txIsolation;
    private volatile boolean autocommit = true;

    // initDB的同时 bind BackendConnecton
    public void initDB(BinaryPacket bin) {
        MySQLMessage mm = new MySQLMessage(bin.data);
        // to skip the packet type
        mm.position(1);
        String db = mm.readString();

        // 检查schema是否已经设置
        if (schema != null) {
            if (schema.equals(db)) {
                writeOk();
            } else {
                schema = db;
                session.changeCurrentSchema(schema);
                writeOk();
            }
            return;
        }
        if (db == null) {
            writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return;
        } else {
            this.schema = db;
            session.changeCurrentSchema(schema);
            writeOk();
            return;
        }

    }

    public void query(BinaryPacket bin) {
        if (queryHandler != null) {
            // 取得语句
            MySQLMessage mm = new MySQLMessage(bin.data);
            mm.position(1);
            String sql = null;
            try {
                sql = mm.readString(charset);
            } catch (UnsupportedEncodingException e) {
                writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
                return;
            }
            if (sql == null || sql.length() == 0) {
                writeErrMessage(ErrorCode.ER_NOT_ALLOWED_COMMAND, "Empty SQL");
                return;
            }

            // 执行查询
            queryHandler.query(sql);
        } else {
            writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Query unsupported!");
        }
    }

    public void close() {
        logger.info("close frontedconnection,host:{},port:{}", host, port);
        ctx.close();
    }

    public void ping() {
        writeOk();
    }

    public void heartbeat(byte[] data) {
        writeOk();
    }

    public void writeOk() {
        ByteBuf byteBuf = ctx.alloc().buffer(OkPacket.OK.length).writeBytes(OkPacket.OK);
        ctx.writeAndFlush(byteBuf);
    }

    public void kill(byte[] data) {
        writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
    }

    public void stmtPrepare(byte[] data) {
        writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
    }

    public void stmtExecute(byte[] data) {
        writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
    }

    public void stmtClose(byte[] data) {
        writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Prepare unsupported!");
    }

    public void writeErrMessage(int errno, String msg) {
        writeErrMessage((byte) 1, errno, msg);
    }

    public void writeBuf(byte[] data) {
        ByteBuf byteBuf = ctx.alloc().buffer(data.length);
        byteBuf.writeBytes(data);
        ctx.writeAndFlush(byteBuf);
        setLastActiveTime();
    }

    public void writeErrMessage(byte id, int errno, String msg) {
        ErrorPacket err = new ErrorPacket();
        err.packetId = id;
        err.errno = errno;
        err.message = encodeString(msg, charset);
        err.write(ctx);
        setLastActiveTime();
    }

    public boolean setCharsetIndex(int ci) {
        String charset = CharsetUtil.getCharset(ci);
        if (charset != null) {
            this.charset = charset;
            this.charsetIndex = ci;
            return true;
        } else {
            return false;
        }
    }

    private final static byte[] encodeString(String src, String charset) {
        if (src == null) {
            return null;
        }
        if (charset == null) {
            return src.getBytes();
        }
        try {
            return src.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            return src.getBytes();
        }
    }

    public void execute(final String sql, final int type) {

        if (type == ServerParse.SELECT) {
            SelectResponse response = session.doQuery(sql);
            response.response(this);
        } else {
            session.execute(sql);
            OkResponse.response(this);
        }
        setLastActiveTime();
    }

    private void doQuery(String sql, int type) {

    }

    public String getCharset() {
        return charset;
    }

    public boolean setCharset(String charset) {
        int ci = CharsetUtil.getIndex(charset);
        if (ci > 0) {
            this.charset = charset;
            this.charsetIndex = ci;
            return true;
        } else {
            return false;
        }
    }

    public void createShema(String stmt) {
        //去掉create
        String s1 = stmt.substring(6, stmt.length()).trim();
        //去掉database
        String schema = s1.substring(8, s1.length()).trim();
        if (StringUtils.isEmpty(schema)) {
            throw new RuntimeException("schema must not empty");
        }
        session.getDatabase().addSchema(schema);
        OkResponse.response(this);
    }

    public void commit() {
        OkResponse.response(this);
    }

    public void rollBack() {
        OkResponse.response(this);
    }

    public void txResponse() {

    }

    public FrontendQueryHandler getQueryHandler() {
        return queryHandler;
    }

    public void setQueryHandler(FrontendQueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    public static int getPacketHeaderSize() {
        return packetHeaderSize;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
        session.changeCurrentSchema(schema);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getLastInsertId() {
        return lastInsertId;
    }

    public void setLastInsertId(long lastInsertId) {
        this.lastInsertId = lastInsertId;
    }

    public static long getAuthTimeout() {
        return AUTH_TIMEOUT;
    }

    public int getTxIsolation() {
        return txIsolation;
    }

    public void setTxIsolation(int txIsolation) {
        this.txIsolation = txIsolation;
    }

    public boolean isAutocommit() {
        return autocommit;
    }

    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime() {
        this.lastActiveTime = (new Date()).getTime();
    }

}
