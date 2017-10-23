package alchemystar.freedom.engine;

import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.meta.Relation;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.transaction.rm.LogRecord;
import alchemystar.freedom.transaction.rm.TransOPAction;
import alchemystar.freedom.transaction.tm.TransStateConst;
import alchemystar.freedom.transaction.tm.Transaction;

/**
 * Session
 *
 * @Author lizhuyang
 */
public class Session {

    private ThreadLocal<Transaction> local = new ThreadLocal<Transaction>();

    // 这边execute的必须是主键索引的tuple
    public void execute(int operation, Relation relation, Tuple tupleBefore, Tuple tupleAfter) {
        preCheck();
        boolean isNotInTransaction = (getState() == TransStateConst.NOT_IN_TRANSACTION);
        if (isNotInTransaction) {
            begin();
        }
        try {
            switch (operation) {
                case TransOPAction.INSERT:
                    insert(relation, tupleAfter);
                    break;
                case TransOPAction.DELETE:
                    delete(relation, tupleBefore);
                    break;
                case TransOPAction.UPDATE:

                    break;
                default:
                    break;
            }
            if (isNotInTransaction) {
                commit();
            }
        } catch (Exception e) {
            rollBack();
        }
    }

    public void insert(Relation relation, Tuple tuple) {
        relation.insert(tuple);
        for (BaseIndex baseIndex : relation.getIndexes()) {
            baseIndex.insert(tuple, baseIndex.isUnique());
        }
        // 插入日志
        getTransaction().insertLog(new LogRecord(getTxId(), TransOPAction.INSERT, null, tuple));
    }

    // 通过tuple的page号和coutn号唯一确定一个tuple
    public void delete(Relation relation, Tuple tuple) {
        relation.delete(tuple);
        for (BaseIndex baseIndex : relation.getIndexes()) {
            baseIndex.remove(tuple);
        }
        // 插入日志
        getTransaction().insertLog(new LogRecord(getTxId(), TransOPAction.DELETE, tuple, null));
    }

    public void update(Relation relation, Tuple tupleBefore, Tuple tupleAfter) {
        relation.update(tupleBefore, tupleAfter);
    }

    public void preCheck() {
        if (local.get() == null) {
            Transaction transaction = new Transaction();
            local.set(transaction);
        }
    }

    public int getTxId() {
        preCheck();
        return local.get().getTxId();
    }

    public int getState() {
        preCheck();
        return local.get().getState();
    }

    public void begin() {
        preCheck();
        local.get().begin();
    }

    public void rollBack() {
        preCheck();
        local.get().rollBack();
        local.remove();
    }

    public void commit() {
        preCheck();
        local.get().commit();
        local.remove();
    }

    public Transaction getTransaction() {
        preCheck();
        return local.get();
    }
}
