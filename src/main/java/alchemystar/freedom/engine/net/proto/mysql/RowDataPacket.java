package alchemystar.freedom.engine.net.proto.mysql;

import java.util.ArrayList;
import java.util.List;

import alchemystar.engine.net.proto.MySQLPacket;
import alchemystar.engine.net.proto.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * RowDataPacket
 *
 * @Author lizhuyang
 */
public class RowDataPacket extends MySQLPacket {
    private static final byte NULL_MARK = (byte) 251;

    public final int fieldCount;
    public final List<byte[]> fieldValues;
    public final List<String> fieldStrings;

    public RowDataPacket(int fieldCount) {
        this.fieldCount = fieldCount;
        this.fieldValues = new ArrayList<byte[]>(fieldCount);
        this.fieldStrings = new ArrayList<String>(fieldCount);
    }

    public void add(byte[] value) {
        fieldValues.add(value);
    }

    public void read(BinaryPacket bin) {
        packetLength = bin.packetLength;
        packetId = bin.packetId;
        MySQLMessage mm = new MySQLMessage(bin.data);
        for (int i = 0; i < fieldCount; i++) {
            byte[] bytes = mm.readBytesWithLength();
            fieldValues.add(bytes);
            fieldStrings.add(new String(bytes));
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.writeByte(packetId);
        for (int i = 0; i < fieldCount; i++) {
            byte[] fv = fieldValues.get(i);
            if (fv == null || fv.length == 0) {
                buffer.writeByte(RowDataPacket.NULL_MARK);
            } else {
                BufferUtil.writeLength(buffer, fv.length);
                buffer.writeBytes(fv);
            }
        }
        ctx.writeAndFlush(buffer);
    }

    @Override
    public ByteBuf writeBuf(ByteBuf buffer, ChannelHandlerContext ctx) {
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.writeByte(packetId);
        for (int i = 0; i < fieldCount; i++) {
            byte[] fv = fieldValues.get(i);
            if (fv == null || fv.length == 0) {
                buffer.writeByte(RowDataPacket.NULL_MARK);
            } else {
                BufferUtil.writeLength(buffer, fv.length);
                buffer.writeBytes(fv);
            }
        }
        return buffer;
    }

    @Override
    public int calcPacketSize() {
        int size = 0;
        for (int i = 0; i < fieldCount; i++) {
            byte[] v = fieldValues.get(i);
            size += (v == null || v.length == 0) ? 1 : BufferUtil.getLength(v);
        }
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL RowData Packet";
    }

}
