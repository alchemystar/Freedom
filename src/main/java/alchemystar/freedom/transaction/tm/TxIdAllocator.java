package alchemystar.freedom.transaction.tm;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事务id分配器
 * TxIdAllocator
 *
 * @Author lizhuyang
 */
public class TxIdAllocator {

    private AtomicInteger txId;

    public TxIdAllocator() {
        // todo load txId from disk
        txId = new AtomicInteger(0);
    }

    public int getNextTxId() {
        return txId.getAndAdd(1);
    }

}
