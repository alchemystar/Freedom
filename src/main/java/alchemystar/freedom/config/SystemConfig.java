package alchemystar.freedom.config;

import alchemystar.freedom.engine.net.proto.util.Isolations;

/**
 * SystemConfig
 *
 * @Author lizhuyang
 */
public interface SystemConfig {

    // todo 不同表,不同文件夹路径

    int DEFAULT_PAGE_SIZE = 4096;

    int DEFAULT_SPECIAL_POINT_LENGTH = 64;

    String RELATION_FILE_PRE_FIX = "/Users/alchemystar/var/freedom/";

    String FREEDOM_REL_PATH = "/Users/alchemystar/var/freedom/t_freedom";

    String FREEDOM_REL_META_PATH = "/Users/alchemystar/var/freedom/t_freedom_meta";

    String FREEDOM_LOG_FILE_NAME = "/Users/alchemystar/var/freedom/t_freedom_log";

    String Database = "";
    // 36小时内连接不发起请求就干掉 秒为单位
    // long IDLE_TIME_OUT = 36 * 3600 * 1000;
    long IDLE_TIME_OUT = 36 * 3600;

    // 1小时做一次idle check 秒为单位
    //int IDLE_CHECK_INTERVAL = 3600 * 1000;
    int IDLE_CHECK_INTERVAL = 3600;

    String DEFAULT_CHARSET = "gbk";

    int DEFAULT_TX_ISOLATION = Isolations.REPEATED_READ;
}
