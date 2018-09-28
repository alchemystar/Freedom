package alchemystar.freedom.engine.net.proto.util;

/**
 * 事务隔离级别定义
 *
 * @Author lizhuyang
 */
public interface Isolations {

    int READ_UNCOMMITTED = 1;
    int READ_COMMITTED = 2;
    int REPEATED_READ = 3;
    int SERIALIZABLE = 4;

}
