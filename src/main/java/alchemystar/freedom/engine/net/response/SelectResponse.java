package alchemystar.freedom.engine.net.response;

import java.util.ArrayList;

import alchemystar.freedom.engine.net.handler.frontend.FrontendConnection;
import alchemystar.freedom.engine.net.proto.mysql.EOFPacket;
import alchemystar.freedom.engine.net.proto.mysql.FieldPacket;
import alchemystar.freedom.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.freedom.engine.net.proto.mysql.RowDataPacket;
import alchemystar.freedom.engine.net.proto.util.Fields;
import alchemystar.freedom.engine.net.proto.util.PacketUtil;
import alchemystar.freedom.engine.net.proto.util.StringUtil;
import alchemystar.freedom.meta.value.Value;
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

    public void responseFields(FrontendConnection c, ByteBuf buffer) {
        buffer = header.writeBuf(buffer, c.getCtx());
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
            buffer = packet.writeBuf(buffer, c.getCtx());
        }
    }

    public void writeEof(FrontendConnection c, ByteBuf buffer) {
        eof.packetId = ++packetId;
        eof.writeBuf(buffer, c.getCtx());
    }

    public void writeLastEof(FrontendConnection c, ByteBuf buffer) {
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        buffer = lastEof.writeBuf(buffer, c.getCtx());
        c.getCtx().writeAndFlush(buffer);
    }

    public void writeRow(Value[] values, FrontendConnection c, ByteBuf buffer) {
        RowDataPacket row = new RowDataPacket(fieldCount);
        for (Value item : values) {
            row.add(StringUtil.encode(item.getString(), c.getCharset()));
        }
        row.packetId = ++packetId;
        row.writeBuf(buffer, c.getCtx());
    }

    public void response(FrontendConnection c) {
        ChannelHandlerContext ctx = c.getCtx();
        ByteBuf buffer = ctx.alloc().buffer();
        responseFields(c, buffer);
        writeEof(c, buffer);
        for (ArrayList<String> item : rows) {
            RowDataPacket row = new RowDataPacket(fieldCount);
            for (String value : item) {
                // 如果两个charset一样,则无需decode
                row.add(StringUtil.encode(value, c.getCharset()));
            }
            row.packetId = ++packetId;
            buffer = row.writeBuf(buffer, ctx);
        }
        writeLastEof(c, buffer);
    }

    public static int convertValueTypeToFieldType(int valueType) {
        if (valueType == Value.BOOLEAN) {
            return Fields.FIELD_TYPE_BIT;
        } else if (valueType == Value.INT) {
            return Fields.FIELD_TYPE_INT24;
        } else if (valueType == Value.LONG) {
            return Fields.FIELD_TYPE_LONG;
        } else if (valueType == Value.STRING) {
            return Fields.FIELD_TYPE_STRING;
        }
        throw new RuntimeException("not support this valueType : " + valueType);
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
