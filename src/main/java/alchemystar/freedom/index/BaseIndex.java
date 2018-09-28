package alchemystar.freedom.index;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.ClusterIndexEntry;
import alchemystar.freedom.meta.IndexDesc;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
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
    protected Table table;
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

    public BaseIndex(Table table, String indexName, Attribute[] attributes) {
        this.table = table;
        this.indexName = indexName;
        this.attributes = attributes;
        path = SystemConfig.RELATION_FILE_PRE_FIX + indexName;
        pageNoAllocator = new PageNoAllocator();
        fStore = new FStore(path);
        fStore.open();
        isUnique = false;
    }

    public IndexEntry buildEntry(IndexEntry entry) {
        // 赋值的过程
        IndexEntry newEntry;
        if (isPrimaryKey) {
            newEntry = new ClusterIndexEntry();
            // cluster索引需要用全部的值
            Attribute[] tableAttributes = table.getAttributes();
            Value[] values = new Value[tableAttributes.length];
            for (int i = 0; i < values.length; i++) {
                String columnName = tableAttributes[i].getName();
                if (entry.getIndexDesc().getAttrsMap().get(columnName) != null) {
                    int oldEntryIndex = entry.getIndexDesc().getAttrsMap().get(columnName).getIndex();
                    values[i] = entry.getValues()[oldEntryIndex];
                }
            }
            newEntry.setValues(values);
            newEntry.setIndexDesc(getIndexDesc());
        } else {
            newEntry = new IndexEntry();
            Value[] values = new Value[attributes.length];
            for (int i = 0; i < values.length; i++) {
                String columnName = attributes[i].getName();
                if (entry.getIndexDesc().getAttrsMap().get(columnName) != null) {
                    int oldEntryIndex = entry.getIndexDesc().getAttrsMap().get(columnName).getIndex();
                    values[i] = entry.getValues()[oldEntryIndex];
                }
            }
            newEntry.setValues(values);
            newEntry.setIndexDesc(getIndexDesc());
        }
        return newEntry;
    }

    public IndexDesc getIndexDesc() {
        IndexDesc indexDesc;
        // 非主键,最后一个是rowId
        if (!isPrimaryKey) {
            indexDesc = new IndexDesc(attributes);
            indexDesc.setPrimaryAttr(attributes[attributes.length - 1]);
        } else {
            // cluster的entry包含所有的属性
            indexDesc = new IndexDesc(table.getAttributes());
            // 主键,第一个是rowId
            // indexDesc.setPrimaryAttr(attributes[0]);
        }
        return indexDesc;
    }

    public int getNextPageNo() {
        return pageNoAllocator.getNextPageNo();
    }

    public void recyclePageNo(int pageNo) {
        pageNoAllocator.recycleCount(pageNo);
    }

    public abstract void flushToDisk();

    // 从tuple中组织出对应索引的key
    public IndexEntry convertToKey(IndexEntry indexEntry) {
        Value[] values = new Value[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            Attribute attribute = attributes[i];
            values[i] = indexEntry.getValues()[attribute.getIndex()];
        }
        return new IndexEntry(values);
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
        if (isPrimaryKey) {
            isUnique = true;
        }
        return this;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
