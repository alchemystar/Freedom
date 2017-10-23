package alchemystar.freedom.transaction.tm;

import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.meta.Relation;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.transaction.rm.LogRecord;
import alchemystar.freedom.transaction.rm.TransOPAction;

/**
 * Transaction
 *
 * @Author lizhuyang
 */
public class Transaction {

    private int txId;

    private Relation relation;

    private TransactionManager manager = TransactionManager.getInstance();
    // 保存事务级别的unRedo
    // 简单版本的事务
    private List<LogRecord> unRedoLog = new ArrayList<LogRecord>();

    private int state = TransStateConst.NOT_IN_TRANSACTION;

    // 开启事务
    public int begin() {
        txId = manager.getNextTxId();
        state = TransStateConst.IN_TRANSACTION;
        return txId;
    }

    // 提交
    public void commit() {
        // todo , flush log
        state = TransStateConst.COMMITED;
        unRedoLog.clear();
    }

    // 回滚
    public void rollBack() {
        state = TransStateConst.ROLLBACK;
        // 从后往前undo
        for (int i = unRedoLog.size(); i >= 0; i++) {
            undo(unRedoLog.get(i));
        }
        unRedoLog.clear();
    }

    // 获取当前事务id
    public int getTxId() {
        return txId;
    }

    public void insertLog(LogRecord record) {
        unRedoLog.add(record);
    }

    public void undo(LogRecord record) {
        // 记录级别的undo
        switch (record.getOperation()) {
            case TransOPAction.DELETE:
                relation.insert(new Item(record.getAfter()));
                break;
            case TransOPAction.INSERT:
                relation.delete(record.getAfter());
                break;
            case TransOPAction.UPDATE:
                relation.delete(record.getAfter());
                relation.insert(record.getBefore());
                break;
            default:
                break;
        }

        // todo key相同的问题
        // todo 方法,每条记录有个唯一记录id
        // 索引级别的redo
        for (BaseIndex index : relation.getIndexes()) {
            Tuple key = index.convertToKey(record.getAfter());
            switch (record.getOperation()) {
                case TransOPAction.INSERT:
                    // delete
                    index.removeOne(key);
                    break;
                case TransOPAction.DELETE:
                    // insert
                    index.insert(key, index.isUnique());
                    break;
                case TransOPAction.UPDATE:
                    // delete after
                    // insert before
                    index.removeOne(key);
                    Tuple beforeKey = index.convertToKey(record.getBefore());
                    index.insert(beforeKey, index.isUnique());
                    break;
                default:
                    break;

            }
        }
    }

    public int getState() {
        return state;
    }

    public Transaction setState(int state) {
        this.state = state;
        return this;
    }

    public Transaction setTxId(int txId) {
        this.txId = txId;
        return this;
    }

}
