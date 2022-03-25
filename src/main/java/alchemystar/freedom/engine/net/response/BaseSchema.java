package alchemystar.freedom.engine.net.response;

import alchemystar.freedom.engine.net.proto.mysql.EOFPacket;
import alchemystar.freedom.engine.net.proto.mysql.FieldPacket;
import alchemystar.freedom.engine.net.proto.mysql.ResultSetHeaderPacket;
import alchemystar.freedom.engine.net.proto.util.PacketUtil;

public class BaseSchema {
    static final int FIELD_COUNT = 1;
    static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    static final EOFPacket eof = new EOFPacket();
}
