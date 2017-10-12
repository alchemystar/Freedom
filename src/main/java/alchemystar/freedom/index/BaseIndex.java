package alchemystar.freedom.index;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.Relation;
import alchemystar.freedom.store.fs.FStore;
import alchemystar.freedom.store.page.PageNoAllocator;

/**
 * BaseIndex
 *
 * @Author lizhuyang
 */
public abstract class BaseIndex implements Index {

    // 隶属于哪个relation
    protected Relation relation;
    // 索引名称
    protected String indexName;
    // 索引用到的属性项
    protected Attribute[] attributes;
    // 索引所在的文件具体位置
    protected String path;

    protected FStore fStore;

    protected PageNoAllocator pageNoAllocator;

    public BaseIndex() {
    }

    public BaseIndex(Relation relation, String indexName, Attribute[] attributes) {
        this.relation = relation;
        this.indexName = indexName;
        this.attributes = attributes;
        path = SystemConfig.RELATION_FILE_PRE_FIX + indexName;
        pageNoAllocator = new PageNoAllocator();
        fStore = new FStore(path);
        fStore.open();
    }

    public int getNextPageNo() {
        return pageNoAllocator.getNextPageNo();
    }

    public void recyclePageNo(int pageNo) {
        pageNoAllocator.recycleCount(pageNo);
    }

    public abstract void flushToDisk();
}
