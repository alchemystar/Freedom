package alchemystar.freedom.index;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.Relation;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.store.fs.FStore;
import alchemystar.freedom.store.page.PageNoAllocator;

/**
 * BaseIndex
 *
 * @Author lizhuyang
 */
public abstract class BaseIndex implements Index {

    // todo 假设当前的key肯定是唯一的
    // todo 先搞定key为唯一的情况,再解决不唯一的key情况

    // 隶属于哪个relation
    protected Relation relation;
    // 索引名称
    protected String indexName;
    // 索引用到的属性项
    protected Attribute[] attributes;
    // 索引所在的文件具体位置
    protected String path;

    protected FStore fStore;

    protected boolean isUnique;

    protected PageNoAllocator pageNoAllocator;

    // 是否是主索引
    protected boolean isPrimaryKey;

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
        isUnique = false;
        isUnique = true;
    }

    public int getNextPageNo() {
        return pageNoAllocator.getNextPageNo();
    }

    public void recyclePageNo(int pageNo) {
        pageNoAllocator.recycleCount(pageNo);
    }

    public abstract void flushToDisk();

    // 从tuple中组织出对应索引的key
    public Tuple convertToKey(Tuple tuple) {
        Value[] values = new Value[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            values[i] = tuple.getValues()[attribute.getIndex()];
        }
        return new Tuple(values);
    }

    public boolean isUnique() {
        return isUnique;
    }

    public BaseIndex setUnique(boolean unique) {
        isUnique = unique;
        return this;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public BaseIndex setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
        return this;
    }
}
