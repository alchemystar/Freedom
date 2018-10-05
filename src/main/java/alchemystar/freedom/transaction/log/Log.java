package alchemystar.freedom.transaction.log;

import alchemystar.freedom.meta.IndexEntry;
import io.netty.buffer.ByteBuf;

/**
 * @Author lizhuyang
 */
public class Log {

    // log的序列号,for 幂等
    private long lsn;

    private int logType;

    // 当前日志对应的trxId
    private int trxId;

    private String tableName;

    private int opType;

    private IndexEntry before;

    private IndexEntry after;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getTrxId() {
        return trxId;
    }

    public void setTrxId(int trxId) {
        this.trxId = trxId;
    }

    public IndexEntry getBefore() {
        return before;
    }

    public void setBefore(IndexEntry before) {
        this.before = before;
    }

    public IndexEntry getAfter() {
        return after;
    }

    public void setAfter(IndexEntry after) {
        this.after = after;
    }

    public int getOpType() {
        return opType;
    }

    public void setOpType(int opType) {
        this.opType = opType;
    }

    public int getLogType() {
        return logType;
    }

    public void setLogType(int logType) {
        this.logType = logType;
    }

    public long getLsn() {
        return lsn;
    }

    public void setLsn(long lsn) {
        this.lsn = lsn;
    }

    public void writeBytes(ByteBuf byteBuf) {
        // for lsn
        byteBuf.writeLong(lsn);
        // for logType
        byteBuf.writeInt(logType);
        // for trxId
        byteBuf.writeInt(trxId);
        if (logType == LogType.ROW) {
            // for table name
            byteBuf.writeInt(tableName.getBytes().length);
            byteBuf.writeBytes(tableName.getBytes());
            //  for opType
            byteBuf.writeInt(opType);
            // for before
            if (before != null) {
                byteBuf.writeInt(before.getLength());
                byteBuf.writeBytes(before.getBytes());
            }
            if (after != null) {
                byteBuf.writeInt(after.getLength());
                byteBuf.writeBytes(after.getBytes());
            }
        }
    }

    @Override
    public String toString() {
        return "Log{" +
                "lsn=" + lsn +
                ", logType=" + logType +
                ", trxId=" + trxId +
                ", tableName='" + tableName + '\'' +
                ", opType=" + opType +
                ", before=" + before +
                ", after=" + after +
                '}';
    }

    //
    //    private int getEntryCount() {
    //        int count = 0;
    //        if (before != null) {
    //            count++;
    //        }
    //        if (after != null) {
    //            count++;
    //        }
    //        return count;
    //    }

    //    private int getLength() {
    //        //  4 for int trxId
    //        //  4 + length for table string'
    //        //  4 for int opType
    //        //  4 for int entry count 1 or 2?
    //        int length = (4) + (4 + tableName.getBytes().length) + (4) + (4);
    //        if (before != null) {
    //            // 4 + indexEntry length
    //            length += (4) + before.getBytes().length;
    //        }
    //        if (after != null) {
    //            // 4 + indexEntry length
    //            length += (4) + after.getBytes().length;
    //        }
    //        return length;
    //    }

}
