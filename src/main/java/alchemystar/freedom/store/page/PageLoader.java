package alchemystar.freedom.store.page;

import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.store.item.ItemPointer;

/**
 * PageLoader
 * 存储了一页page中所有的tuple
 *
 * @Author lizhuyang
 */
public class PageLoader {

    Page page;
    private Tuple[] tuples;
    private int tupleCount;

    public PageLoader(Page page) {
        this.page = page;
    }

    public void load() {
        PageHeaderData pageHeaderData = PageHeaderData.read(page);
        tupleCount = pageHeaderData.getTupleCount();
        int ptrStartOff = pageHeaderData.getLength();
        // 首先建立存储tuple的数组
        List<Tuple> temp = new ArrayList<Tuple>();
        // 循环读取
        for (int i = 0; i < tupleCount; i++) {
            // 重新从page读取tuple
            ItemPointer ptr = new ItemPointer(page.readInt(), page.readInt());
            if (ptr.getTupleLength() == -1) {
                continue;
            }
            byte[] bb = page.readBytes(ptr.getOffset(), ptr.getTupleLength());
            Tuple tuple = new Tuple();
            tuple.read(bb);
            temp.add(tuple);
            // 进入到下一个元组位置
            ptrStartOff = ptrStartOff + ptr.getTupleLength();
        }
        // 由于可能由于被删除,置为-1,所以以temp为准
        tuples = temp.toArray(new Tuple[temp.size()]);
        tupleCount = temp.size();
    }

    public Tuple[] getTuples() {
        return tuples;
    }

    public int getTuplCount() {
        return tupleCount;
    }

}
