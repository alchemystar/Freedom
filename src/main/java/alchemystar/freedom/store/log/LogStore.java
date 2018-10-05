package alchemystar.freedom.store.log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.store.fs.FileUtils;
import alchemystar.transaction.OpType;
import alchemystar.transaction.log.Log;
import alchemystar.transaction.log.LogType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

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

    private ByteBufAllocator byteBufAllocator;

    public LogStore() {
        this.logPath = SystemConfig.FREEDOM_LOG_FILE_NAME;
        byteBufAllocator = new UnpooledByteBufAllocator(false);
        open();
    }

    public void open() {
        fileChannel = FileUtils.open(logPath);
    }

    public void close() {
        FileUtils.closeFile(fileChannel);
    }

    public void appendLog(Log log) {
        ByteBuf byteBuf = byteBufAllocator.buffer(1024);
        log.writeBytes(byteBuf);
        append(byteBuf.nioBuffer());
    }

    // for 重新启动时候使用
    public List<Log> loadLog() {
        // 从文件开始load
        try {
            fileChannel.position(0);
            long length = fileChannel.size();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) length);
            FileUtils.readFully(fileChannel, byteBuffer);
            return readAllLog(byteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Log> readAllLog(ByteBuffer byteBuffer) {
        List<Log> logs = new ArrayList<Log>();
        ByteBuf byteBuf = byteBufAllocator.buffer(byteBuffer.capacity());
        byteBuf.writeBytes(byteBuffer.array());

        while (byteBuf.readableBytes() > 0) {
            Log log = new Log();
            // logType
            log.setLogType(byteBuf.readInt());
            // trxId
            log.setTrxId(byteBuf.readInt());
            // 只有row的日志才有记录,其它只是记录了其标识
            if (log.getLogType() == LogType.ROW) {
                int tableNameLength = byteBuf.readInt();
                byte[] byteName = new byte[tableNameLength];
                byteBuf.readBytes(byteName);
                String tableName = new String(byteName);
                log.setTableName(tableName);
                log.setOpType(byteBuf.readInt());
                int length = byteBuf.readInt();
                byte[] entryByte = new byte[length];
                byteBuf.readBytes(entryByte);
                IndexEntry entry = new IndexEntry();
                entry.read(entryByte);
                if (log.getOpType() == OpType.insert) {
                    log.setAfter(entry);
                } else if (log.getOpType() == OpType.delete) {
                    log.setAfter(entry);
                }
            }
            logs.add(log);
        }
        return logs;
    }

    // 添加日志
    public void append(ByteBuffer dst) {
        try {
            FileUtils.append(fileChannel, dst);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
