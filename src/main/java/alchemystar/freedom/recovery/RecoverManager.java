package alchemystar.freedom.recovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alchemystar.freedom.store.log.LogStore;
import alchemystar.freedom.transaction.Trx;
import alchemystar.freedom.transaction.TrxManager;
import alchemystar.freedom.transaction.log.Log;
import alchemystar.freedom.transaction.log.LogType;

/**
 * @Author lizhuyang
 */
public class RecoverManager {

    private LogStore logStore;

    // 先简单的使用redo log 进行redo
    public void recover() {
        // 首先由logStore加载出所有的log
        List<Log> list = logStore.loadLog();
        // 找出所有已经提交的事务
        List<Trx> trxList = getAllCommittedTrx(list);
        // 然后进行redo操作
        for (Trx trx : trxList) {
            trx.redo();
        }
    }

    public List<Trx> getAllCommittedTrx(List<Log> list) {
        List<Trx> trxList = new ArrayList<Trx>();
        Map<Integer, Trx> trxMap = new HashMap<Integer, Trx>();
        // 找出所有已提交的trx
        for (Log log : list) {
            if (log.getLogType() == LogType.TRX_START) {
                Trx trx = TrxManager.newEmptyTrx();
                trx.setTrxId(log.getTrxId());
                trx.addLog(log);
                trxMap.put(trx.getTrxId(), trx);
            } else if (log.getLogType() == LogType.ROW) {
                trxMap.get(log.getTrxId()).addLog(log);
            } else if (log.getLogType() == LogType.COMMIT) {
                trxMap.get(log.getTrxId()).addLog(log);
                // 按照先commit的顺序来
                trxList.add(trxMap.get(log.getTrxId()));
            }
        }

        return trxList;
    }

    public LogStore getLogStore() {
        return logStore;
    }

    public void setLogStore(LogStore logStore) {
        this.logStore = logStore;
    }
}
