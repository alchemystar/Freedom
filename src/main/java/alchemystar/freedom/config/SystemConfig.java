package alchemystar.freedom.config;

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
}
