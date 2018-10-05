package alchemystar.transaction;

import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.engine.Database;
import alchemystar.freedom.meta.ClusterIndexEntry;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.transaction.log.Log;
import alchemystar.transaction.log.LogType;
import alchemystar.transaction.undo.UndoManager;

/**
 * 事务
 *
 * @Author lizhuyang
 */
public class Trx {
    // 事务状态,初始化为 事务未开始
    private int state = TrxState.TRX_STATE_NOT_STARTED;
    // 事务id
    private int trxId;

    private List<Log> logs = new ArrayList<Log>();

    public void begin() {
        state = TrxState.TRX_STATE_ACTIVE;
    }

    // 都是Row模式下的add
    public void addLog(Table table, int opType, IndexEntry before, IndexEntry after) {
        if (!(before == null || before instanceof ClusterIndexEntry) || !(after == null || after instanceof
                ClusterIndexEntry)) {
            throw new RuntimeException("log before and after must be clusterIndexEntry");
        }
        Log log = new Log();
        log.setLogType(LogType.ROW);
        log.setTrxId(trxId);
        log.setOpType(opType);
        log.setTableName(table.getName());
        log.setBefore(before);
        log.setAfter(after);
        logs.add(log);

    }

    public void commit() {
        // logs 落盘
        for (Log log : logs) {
            Database.getInstance().getLogStore().appendLog(log);
        }
        // 加上commit日志
        Log commitLog = new Log();
        commitLog.setTrxId(trxId);
        commitLog.setLogType(LogType.COMMIT);
        Database.getInstance().getLogStore().appendLog(commitLog);
        state = TrxState.TRX_COMMITTED;
        // commit 之后无法使用undoLog
        logs.clear();
    }

    public void rollback() {
        undo();
        state = TrxState.TRX_STATE_NOT_STARTED;
        // rollback之后无法使用undoLog
        logs.clear();
    }

    public int getTrxId() {
        return trxId;
    }

    public void setTrxId(int trxId) {
        this.trxId = trxId;
    }

    private void undo() {
        // 反序undo
        for (int i = logs.size() - 1; i >=0; i--) {
            UndoManager.undo(logs.get(i));
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean trxIsNotStart() {
        return state == TrxState.TRX_STATE_NOT_STARTED;
    }
}
