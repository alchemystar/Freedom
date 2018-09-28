package alchemystar.freedom.store.page;

import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.store.item.ItemPointer;

/**
 * PageLoader
 * 存储了一页page中所有的tuple
 *
 * @Author lizhuyang
 */
public class PageLoader {

    Page page;
    private IndexEntry[] indexEntries;
    private int tupleCount;

    public PageLoader(Page page) {
        this.page = page;
    }

    public void load() {
        PageHeaderData pageHeaderData = PageHeaderData.read(page);
        tupleCount = pageHeaderData.getTupleCount();
        int ptrStartOff = pageHeaderData.getLength();
        // 首先建立存储tuple的数组
        List<IndexEntry> temp = new ArrayList<IndexEntry>();
        // 循环读取
        for (int i = 0; i < tupleCount; i++) {
            // 重新从page读取tuple
            ItemPointer ptr = new ItemPointer(page.readInt(), page.readInt());
            if (ptr.getTupleLength() == -1) {
                continue;
            }
            byte[] bb = page.readBytes(ptr.getOffset(), ptr.getTupleLength());
            IndexEntry indexEntry = new IndexEntry();
            indexEntry.read(bb);
            temp.add(indexEntry);
            // 进入到下一个元组位置
            ptrStartOff = ptrStartOff + ptr.getTupleLength();
        }
        // 由于可能由于被删除,置为-1,所以以temp为准
        indexEntries = temp.toArray(new IndexEntry[temp.size()]);
        tupleCount = temp.size();
    }

    public IndexEntry[] getIndexEntries() {
        return indexEntries;
    }

    public int getTuplCount() {
        return tupleCount;
    }

}
