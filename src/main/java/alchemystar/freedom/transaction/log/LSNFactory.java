package alchemystar.freedom.transaction.log;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author lizhuyang
 */
public class LSNFactory {

    private static AtomicLong lsnAllocator = new AtomicLong(0);

    public static long nextLSN() {
        return lsnAllocator.getAndIncrement();
    }
}
