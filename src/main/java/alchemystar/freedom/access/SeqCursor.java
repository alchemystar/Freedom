package alchemystar.freedom.access;

import alchemystar.freedom.meta.Relation;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.store.page.Page;
import alchemystar.freedom.store.page.PageLoader;

/**
 * SeqCursor
 * 顺序扫描
 *
 * @Author lizhuyang
 */
public class SeqCursor implements Cursor {

    private Relation relation;
    // 当前的page,从第一页开始,第0页为PageOffset页
    private int currentPageNo = 1;
    // page中的count
    private int currentTupleCount = 0;

    private PageLoader currentPageLoader;

    public SeqCursor(Relation relation) {
        this.relation = relation;
    }

    @Override
    public Tuple getNext() {
        if (currentPageLoader == null) {
            loadPage(currentPageNo);
        }
        if (currentTupleCount < currentPageLoader.getTuplCount()) {
            return currentPageLoader.getTuples()[currentTupleCount++];
        } else {
            // 表明需要到下一页
            currentTupleCount = 0;
            currentPageNo++;
            if (currentPageNo > relation.getPageCount()) {
                // 如果超过了relation的页数,则返回null
                return null;
            }
            loadPage(currentPageNo);
            return currentPageLoader.getTuples()[currentTupleCount++];
        }
    }

    private void loadPage(int pageNo) {
        Page page = relation.getPageMap().get(currentPageNo);
        currentPageLoader = new PageLoader(page);
        currentPageLoader.load();
    }
}
