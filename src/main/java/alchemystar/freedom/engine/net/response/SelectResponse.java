package alchemystar.freedom.engine.net.response;

import java.util.ArrayList;

import alchemystar.engine.net.handler.frontend.FrontendConnection;
import alchemystar.engine.net.proto.mysql.EOFPacket;
import alchemystar.engine.net.proto.mysql.FieldPacket;
import alchemystar.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.engine.net.proto.mysql.RowDataPacket;
import alchemystar.engine.net.proto.util.Fields;
import alchemystar.engine.net.proto.util.PacketUtil;
import alchemystar.engine.net.proto.util.StringUtil;
import alchemystar.value.Value;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author lizhuyang
 */
public class SelectResponse {

    private Integer fieldCount;
    private ResultSetHeaderPacket header;
    private ArrayList<Field> fields;
    private ArrayList<ArrayList<String>> rows;
    private static final EOFPacket eof = new EOFPacket();
    private byte packetId;
    private String originCharset;

    public SelectResponse(Integer fieldCount) {
        this.fieldCount = fieldCount;
        header = PacketUtil.getHeader(fieldCount);
        header.packetId = ++packetId;
        fields = new ArrayList<Field>();
        rows = new ArrayList<ArrayList<String>>();
    }

    public void addField(String fieldName, int type) {
        Field field = new Field(fieldName, type);
        fields.add(field);
    }

    public void response(FrontendConnection c) {
        ChannelHandlerContext ctx = c.getCtx();
        ByteBuf buffer = ctx.alloc().buffer();
        buffer = header.writeBuf(buffer, ctx);
        for (Field field : fields) {
            FieldPacket packet = null;
            if (field.getType() == Value.LONG) {
                packet = PacketUtil.getField(field.getFieldName(), Fields.FIELD_TYPE_LONGLONG);
            } else if (field.getType() == Value.INT) {
                // int to long
                packet = PacketUtil.getField(field.getFieldName(), Fields.FIELD_TYPE_LONG);
            } else {
                packet = PacketUtil.getField(field.getFieldName(), Fields.FIELD_TYPE_VAR_STRING);
            }
            packet.packetId = ++packetId;
            buffer = packet.writeBuf(buffer, ctx);
        }
        eof.packetId = ++packetId;
        buffer = eof.writeBuf(buffer, ctx);
        for (ArrayList<String> item : rows) {
            RowDataPacket row = new RowDataPacket(fieldCount);
            for (String value : item) {
                // 如果两个charset一样,则无需decode
                row.add(StringUtil.encode(value, c.getCharset()));
            }
            row.packetId = ++packetId;
            buffer = row.writeBuf(buffer, ctx);
        }
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, ctx);
        ctx.writeAndFlush(buffer);
    }

    public ArrayList<ArrayList<String>> getRows() {
        return rows;
    }

    public void setRows(ArrayList<ArrayList<String>> rows) {
        this.rows = rows;
    }

    private class Field {
        private String fieldName;
        private int type;

        public Field(String fieldName, int type) {
            this.fieldName = fieldName;
            this.type = type;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public String getOriginCharset() {
        return originCharset;
    }

    public void setOriginCharset(String originCharset) {
        this.originCharset = originCharset;
    }
}
