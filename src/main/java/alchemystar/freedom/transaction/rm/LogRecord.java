package alchemystar.freedom.transaction.rm;

import java.nio.ByteBuffer;

import alchemystar.freedom.meta.Tuple;

/**
 * LogRecord
 *
 * @Author lizhuyang
 */
public class LogRecord {
    // 日志记录头
    private LogRecordHeader header;
    // insert/update/delete commit/rollback
    private int operation;
    // 动作之前
    private Tuple before;
    // 动作之后
    private Tuple after;

    public LogRecord(int txId, int operation, Tuple before, Tuple after) {
        header.setTxId(txId);
        this.operation = operation;
        this.before = before;
        this.after = after;
        this.operation = operation;
        header.setLength(getLength());
    }

    public ByteBuffer getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getLength());
        buffer.put(header.getBytes());
        // tuple before length
        if (before == null) {
            buffer.putInt(0);
        } else {
            buffer.putInt(before.getLength());
            buffer.put(before.getBytes());
        }
        if (after == null) {
            buffer.putInt(0);
        } else {
            // tuple after length
            buffer.putInt(after.getLength());
            buffer.put(after.getBytes());
        }
        return buffer;
    }

    public int getLength() {
        int length = LogRecordHeader.getLength();
        // for operation
        length += 4;
        length += 4 + before.getLength();
        length += 4 + after.getLength();
        return length;
    }

    public int getOperation() {
        return operation;
    }

    public LogRecord setOperation(int operation) {
        this.operation = operation;
        return this;
    }

    public Tuple getBefore() {
        return before;
    }

    public LogRecord setBefore(Tuple before) {
        this.before = before;
        return this;
    }

    public Tuple getAfter() {
        return after;
    }

    public LogRecord setAfter(Tuple after) {
        this.after = after;
        return this;
    }
}
