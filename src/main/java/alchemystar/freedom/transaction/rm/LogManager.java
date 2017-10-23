package alchemystar.freedom.transaction.rm;

import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.store.log.LogStore;

/**
 * LogManager
 *
 * @Author lizhuyang
 */
public class LogManager {

    private LogStore logStore;

    private LSN maxLsn;

    private List<LogRecord> unReLog = new ArrayList<LogRecord>();

    public void load() {
        // 从logStore中读取文件
    }

    public LogRecord read(LSN lsn) {
        return null;
    }

    public void insert(LogRecord logRecord) {
        unReLog.add(logRecord);
    }

    // 刷掉的最大lsn
    public LSN flush() {
        // 刷日志
        // flush 数据 to write
        // todo
        return null;
    }
}
