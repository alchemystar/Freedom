package alchemystar.freedom.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.store.fs.FStore;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.store.page.Page;
import alchemystar.freedom.store.page.PageLoader;
import alchemystar.freedom.store.page.PagePool;
import alchemystar.freedom.util.ValueConvertUtil;

/**
 * Relation
 * 即Table
 *
 * @Author lizhuyang
 */
public class Relation {
    // relation包含的元组描述
    private TupleDesc tupleDesc;
    // Relation对应的FilePath
    private String relPath;
    // Relation对应的metaPath
    private String metaPath;
    // page的数量,初始化为1,因为0是pageOffSet信息
    private int pageCount = 1;
    // pageCount是否change,if change,则更新pageOffSet信息
    private boolean isPageCountDirty;
    // 装载具体数据信息
    private FStore relStore;
    // 装载原信息
    private FStore metaStore;
    // 页号/偏移 映射
    private Map<Integer, Integer> pageOffsetMap;
    // 页号/PageLoad映射
    private Map<Integer, Page> pageMap;

    // 索引集合,包含了primaryIndex
    private List<BaseIndex> indexes = new ArrayList<BaseIndex>();
    // 主键索引,聚簇索引
    // 主键索引,最后两个必须是pageNo,countNo
    private BaseIndex primaryIndex;

    public static final int META_PAGE_INDEX = 0;

    public static final int PAGE_OFFSET_INDEX = 0;

    public Relation() {

    }

    public void insert(Tuple tuple) {
        insert(new Item(tuple));
    }

    public void delete(Tuple tuple) {
        // todo 这样删除会留下空洞,重复利用数据check
        // 能删除的tuple必须是从主键索引查出来的tuple
        // 如果从其它索引查tuple,则会再查一次主键tuple,这样的话,能知道其pageNo以及pageCount
        Page page = relStore.readPageFromFile(getPageNo(tuple));
        page.delete(getPageCount(tuple));
    }

    // 主键tuple的最后两个,一个是pageCount,一个是pageNo
    private int getPageNo(Tuple tuple) {
        int length = tuple.getLength();
        return ((ValueInt) (tuple.getValues()[length - 2])).getInt();
    }

    private int getPageCount(Tuple tuple) {
        int length = tuple.getLength();
        return ((ValueInt) (tuple.getValues()[length - 1])).getInt();
    }

    // 这边用的是新的update过后的tuple
    // 删除的时候根据pageNo和pageCount删除旧tuple
    // 再插入新的tuple
    public void update(Tuple tupleBefore, Tuple tupleAfter) {
        // 首先将原先的tuple给删除,其中之用到了tuple中的pageNo和pageCount字段
        delete(tupleBefore);
        // 然后将新的tuple给写入
        insert(tupleAfter);
    }

    public void insert(Item item) {
        int itemLength = item.getLength();
        // 从已有的页文件中,寻找一个足够空间的进行内容的插入
        int pageNo = findEnoughSpace(itemLength);
        if (pageNo > 0) {
            pageMap.get(pageNo).writeItem(item);
            return;
        }
        // 表明已经没有足够的空间进行插入
        // 新申请一页
        Page page = mallocPage();
        if (page.remainFreeSpace() < itemLength) {
            throw new RuntimeException("item size too long");
        }
        page.writeItem(item);
    }

    public void loadFromDisk() {
        readMeta();
        readPageOffInfo();
        pageMap.clear();
        // 通过pageOffSet重新加载数据
        for (int pageNo : pageOffsetMap.keySet()) {
            pageMap.put(pageNo, relStore.readPageFromFile(pageNo));
        }
    }

    public void flushToDisk() {
        if (isPageCountDirty) {
            // 如果pageCount改动过,flush pageCount
            writePageOffInfo();
        }
        for (Integer pageNo : pageMap.keySet()) {
            Page page = pageMap.get(pageNo);
            // 如果页有改动,则flush
            if (page.isDirty()) {
                relStore.writePageToFile(page, pageNo);
                // 写完后,则页不脏
                page.setDirty(false);
            }
        }
    }

    private int findEnoughSpace(int needSpace) {
        for (Integer pageNo : pageMap.keySet()) {
            if (pageMap.get(pageNo).remainFreeSpace() >= needSpace) {
                return pageNo;
            }
        }
        return -1;
    }

    private Page mallocPage() {
        // 从pool中获取page
        Page page = PagePool.getIntance().getFreePage();
        pageMap.put(pageCount, page);
        pageOffsetMap.put(pageCount, pageCount * SystemConfig.DEFAULT_PAGE_SIZE);
        incrPageCount();
        return page;
    }

    public void open() {
        relStore = new FStore(relPath);
        relStore.open();
        metaStore = new FStore(metaPath);
        metaStore.open();
        // 初始化pageOffsetMap
        pageOffsetMap = new HashMap<Integer, Integer>();
        pageMap = new HashMap<Integer, Page>();
    }

    // 读relation的原信息
    public void readMeta() {
        // 元信息只有一页
        PageLoader loader = metaStore.readPageLoaderFromFile(0);
        List<Attribute> list = new ArrayList<Attribute>();
        for (Tuple tuple : loader.getTuples()) {
            Attribute attr = ValueConvertUtil.convertValue(tuple.getValues());
            list.add(attr);
        }
        tupleDesc = new TupleDesc(list.toArray(new Attribute[list.size()]));
    }

    // 读取页page offset映射信息
    public void readPageOffInfo() {
        // page offset信息仅仅在第一页
        PageLoader loader = relStore.readPageLoaderFromFile(0);
        pageOffsetMap.clear();
        pageCount = loader.getTuples().length;
        for (Tuple tuple : loader.getTuples()) {
            Value[] values = tuple.getValues();
            int pageNo = ((ValueInt) values[0]).getInt();
            int offset = ((ValueInt) values[1]).getInt();
            pageOffsetMap.put(pageNo, offset);
        }
    }

    // 写入页page offset映射信息
    public void writePageOffInfo() {
        List<Item> list = new ArrayList<Item>();
        for (Integer pageNo : pageOffsetMap.keySet()) {
            Value[] values = new Value[2];
            values[0] = new ValueInt(pageNo);
            values[1] = new ValueInt(pageOffsetMap.get(pageNo));
            Tuple tuple = new Tuple(values);
            list.add(new Item(tuple));
        }
        Page page = PagePool.getIntance().getFreePage();
        page.writeItems(list);
        relStore.writePageToFile(page, PAGE_OFFSET_INDEX);
    }

    // 写relation的元信息
    public void writeMeta() {
        Page page = convertToMetaPage();
        metaStore.writePageToFile(page, META_PAGE_INDEX);
    }

    public Page convertToMetaPage() {
        // 首先获取需要写入的原信息
        List<Item> list = tupleDesc.getItems();
        Page page = PagePool.getIntance().getFreePage();
        page.writeItems(list);
        return page;
    }

    public String getRelPath() {
        return relPath;
    }

    public Relation setRelPath(String relPath) {
        this.relPath = relPath;
        return this;
    }

    public String getMetaPath() {
        return metaPath;
    }

    public Relation setMetaPath(String metaPath) {
        this.metaPath = metaPath;
        return this;
    }

    public TupleDesc getTupleDesc() {
        return tupleDesc;
    }

    public Relation setTupleDesc(TupleDesc tupleDesc) {
        this.tupleDesc = tupleDesc;
        return this;
    }

    public void close() {
        relStore.close();
        metaStore.close();
    }

    public void incrPageCount() {
        pageCount++;
        isPageCountDirty = true;
    }

    public Map<Integer, Page> getPageMap() {
        return pageMap;
    }

    public Relation setPageMap(Map<Integer, Page> pageMap) {
        this.pageMap = pageMap;
        return this;
    }

    public int getPageCount() {
        return pageCount;
    }

    public Relation setPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    public List<BaseIndex> getIndexes() {
        return indexes;
    }

    public Relation setIndexes(List<BaseIndex> indexes) {
        this.indexes = indexes;
        return this;
    }
}
