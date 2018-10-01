package alchemystar.transaction;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author lizhuyang
 */
public class TrxManager {

    private static AtomicInteger trxIdCount = new AtomicInteger(1);

    public static Trx newTrx() {
        Trx trx = new Trx();
        trx.setTrxId(trxIdCount.getAndIncrement());
        return trx;
    }
}
