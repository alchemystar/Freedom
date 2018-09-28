package alchemystar.freedom.engine.net.proto.util;

/**
 * @Author lizhuyang
 */
public interface Versions {
    /** 协议版本 */
    byte PROTOCOL_VERSION = 10;

    /** 服务器版本 */
    byte[] SERVER_VERSION = "5.1.1-freedom".getBytes();
}
