package alchemystar.freedom.transaction.rm;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * 日志记录头
 *
 * @Author lizhuyang
 */
public class LogRecordHeader {
    // todo 一些字段暂时不用
    // 当前record的总长度,除当前的4字节length之外
    private int length;
    private LSN lsn;
    // todo 暂时用不到
    private LSN prevLsn;
    private Long timeStamp = (new Date()).getTime();
    // 写此条log记录的资源管理器id
    // 不一定用的到
    private int rmId;
    // 写当前记录的事务id
    private int txId;
    // 该事务的前一个日志记录的lsn
    private LSN tranPrevLsn;

    public ByteBuffer getBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        byteBuffer.putInt(length);
        byteBuffer.putInt(lsn.getRba());
        // for prevLsb
        byteBuffer.putInt(0);//prevLsn.getRba());
        byteBuffer.putLong(timeStamp);
        byteBuffer.putInt(rmId);
        byteBuffer.putInt(txId);
        byteBuffer.putInt(tranPrevLsn.getRba());
        return byteBuffer;
    }

    public int getTxId() {
        return txId;
    }

    public LogRecordHeader setTxId(int txId) {
        this.txId = txId;
        return this;
    }

    public LogRecordHeader setLength(int length) {
        this.length = length;
        return this;
    }

    public static int getLength() {
        return 4 + 4 + 4 + 8 + 4 + 4 + 4;
    }

}
