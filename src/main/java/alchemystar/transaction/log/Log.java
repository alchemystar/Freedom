package alchemystar.transaction.log;

import alchemystar.freedom.meta.IndexEntry;

/**
 * @Author lizhuyang
 */
public class Log {

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
}
