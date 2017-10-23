package alchemystar.freedom.store.log;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import alchemystar.freedom.store.fs.FileUtils;

/**
 * 文件路径
 *
 * @Author lizhuyang
 */
public class LogStore {

    // 文件路径
    private String logPath;
    // 文件channel
    private FileChannel fileChannel;

    public LogStore(String logPath) {
        this.logPath = logPath;
    }

    public void open() {
        fileChannel = FileUtils.open(logPath);
    }

    public void close() {
        FileUtils.closeFile(fileChannel);
    }

    // 添加日志
    public void append(ByteBuffer dst) {
        try {
            FileUtils.writeFully(fileChannel, dst);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
