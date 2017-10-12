package alchemystar.freedom.store.page;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PageNoAllocator
 *
 * @Author lizhuyang
 */
public class PageNoAllocator {

    private AtomicInteger count;

    // todo thread-safe
    private List<Integer> freePageNoList;

    public PageNoAllocator() {
        count = new AtomicInteger(0);
        freePageNoList = new LinkedList<Integer>();
    }

    public int getNextPageNo() {
        if (freePageNoList.size() == 0) {
            return count.getAndAdd(1);
        }
        return freePageNoList.remove(0);
    }

    public void recycleCount(int pageNo) {
        freePageNoList.add(pageNo);
    }

    // 从磁盘中,重新构造page的时候,需要重新设置其pageNo
    public void setCount(int lastPageNo) {
        count.set(lastPageNo + 1);
    }
}
