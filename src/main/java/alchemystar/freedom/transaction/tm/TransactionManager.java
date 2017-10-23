package alchemystar.freedom.transaction.tm;

/**
 * TransactionManager
 *
 * @Author lizhuyang
 */
public class TransactionManager {

    private static TransactionManager manager;

    private TxIdAllocator txIdAllocator;

    static {
        manager = new TransactionManager();
    }

    private TransactionManager() {
        txIdAllocator = new TxIdAllocator();
    }

    public int getNextTxId() {
        return txIdAllocator.getNextTxId();
    }

    public static TransactionManager getInstance() {
        return manager;
    }

}
