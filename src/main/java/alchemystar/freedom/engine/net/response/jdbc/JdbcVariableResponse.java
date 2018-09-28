package alchemystar.freedom.engine.net.response.jdbc;

import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.EOFPacket;
import alchemystar.engine.net.proto.mysql.FieldPacket;
import alchemystar.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.engine.net.proto.mysql.RowDataPacket;
import alchemystar.engine.net.proto.util.Fields;
import alchemystar.engine.net.proto.util.PacketUtil;
import alchemystar.engine.net.proto.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * JdbcVariableResponsez
 *
 * @Author lizhuyang
 */
public class JdbcVariableResponse {

    private static final int FIELD_COUNT = 2;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("Variable_name", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        fields[i] = PacketUtil.getField("Value", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        eof.packetId = ++packetId;
    }

    public static void response(FrontendConnection c) {
        ChannelHandlerContext ctx = c.getCtx();
        ByteBuf buffer = ctx.alloc().buffer();

        // write header
        buffer = header.writeBuf(buffer, ctx);

        // write fields
        for (FieldPacket field : fields) {
            buffer = field.writeBuf(buffer, ctx);
        }

        // write eof
        buffer = eof.writeBuf(buffer, ctx);

        // write rows
        Byte packetId = eof.packetId;
        RowDataPacket row = null;
        row = genOneRow("character_set_client", "gb2312", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("character_set_connection", "gb2312", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("character_set_results", "gb2312", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("character_set_server", "latin1", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("init_connect", "", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("interactive_timeout", "28800", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("lower_case_table_names", "2", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("max_allowed_packet", "4194304", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("net_buffer_length", "16384", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("net_write_timeout", "60", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("query_cache_size", "1048576", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("query_cache_type", "OFF", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("sql_mode", "STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("system_time_zone", "CST", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("time_zone", "SYSTEM", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("tx_isolation", "REPEATABLE-READ", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);
        row = genOneRow("wait_timeout", "28800", c);
        row.packetId = ++packetId;
        buffer = row.writeBuf(buffer, ctx);

        // write lastEof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);

        // write buffer
        ctx.writeAndFlush(buffer);
    }

    private static RowDataPacket genOneRow(String value1, String value2, FrontendConnection c) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode(value1, c.getCharset()));
        row.add(StringUtil.encode(value2, c.getCharset()));
        return row;
    }
}
