package alchemystar.freedom.index.bp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alchemystar.freedom.access.ClusterIndexCursor;
import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.access.SecondIndexCursor;
import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.index.CompareType;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.store.page.Page;
import alchemystar.freedom.store.page.PageLoader;
import alchemystar.freedom.store.page.PagePool;

/**
 * BPTree
 * B plus tree
 *
 * @Author lizhuyang
 */
public class BPTree extends BaseIndex {

    /**
     * 根节点
     */
    protected BPNode root;

    /**
     * 叶子节点的链表头
     */
    protected BPNode head;

    protected Map<Integer, BPNode> nodeMap;

    public BPTree(Table table, String indexName, Attribute[] attributes) {
        super(table, indexName, attributes);
        root = new BPNode(true, true, this);
        head = root;
        nodeMap = new HashMap<Integer, BPNode>();
    }

    public void loadFromDisk() {
        int rootPageNo = getRootPageNoFromMeta();
        getNodeFromPageNo(rootPageNo);
    }

    public int getRootPageNoFromMeta() {
        PageLoader loader = new PageLoader(fStore.readPageFromFile(0));
        loader.load();
        return ((ValueInt) loader.getIndexEntries()[0].getValues()[0]).getInt();
    }

    public BPNode getNodeFromPageNo(int pageNo) {
        if (pageNo == -1) {
            return null;
        }
        BPNode bpNode = nodeMap.get(pageNo);
        if (bpNode != null) {
            return bpNode;
        }
        BpPage bpPage = (BpPage) fStore.readPageFromFile(pageNo, true);
        bpNode = bpPage.readFromPage(this);
        if (bpNode.isRoot()) {
            root = bpNode;
        }
        if (bpNode.isLeaf() && bpNode.getPrevious() == null) {
            head = bpNode;
        }
        return bpNode;
    }

    @Override
    public Cursor searchEqual(IndexEntry key) {
        Position startPosition = getFirst(key, CompareType.EQUAL);
        if (startPosition == null) {
            return null;
        }
        startPosition.setSearchEntry(key);
        if (isPrimaryKey) {
            return new ClusterIndexCursor(startPosition, null, true);
        } else {
            SecondIndexCursor cursor = new SecondIndexCursor(startPosition, null, true);
            cursor.setClusterIndex(table.getClusterIndex());
            return cursor;
        }
    }

    @Override
    public Cursor searchRange(IndexEntry lowKey, IndexEntry upKey) {
        Position startPosition = getFirst(lowKey, CompareType.LOW);
        if (startPosition == null) {
            return null;
        }
        Position endPosition = null;
        if (upKey != null) {
            startPosition.setSearchEntry(lowKey);
            if (upKey != null) {
                endPosition = getLast(upKey, CompareType.UP);
            }
            if (endPosition != null) {
                endPosition.setSearchEntry(upKey);
            }
        }
        if (isPrimaryKey) {
            return new ClusterIndexCursor(startPosition, endPosition, false);
        } else {
            SecondIndexCursor cursor = new SecondIndexCursor(startPosition, endPosition, false);
            cursor.setClusterIndex(table.getClusterIndex());
            return cursor;
        }
    }

    @Override
    public Position getFirst(IndexEntry outKey, int CompareType) {
        IndexEntry key = buildEntry(outKey);
        Position position = root.get(key.getCompareEntry(), CompareType);
        if (position == null) {
            return null;
        }
        // 由于存在key大量一样的情况,所以必须往前遍历,因为前面也可能有相同的key;
        BPNode bpNode = position.getBpNode().getPrevious();
        while (bpNode != null) {
            // 从后往前倒查找
            for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
                IndexEntry item = bpNode.getEntries().get(i);
                if (item.compareIndex(key) == 0) {
                    position.setBpNode(bpNode);
                    position.setPosition(i);
                }
                if (!item.equals(key)) {
                    break;
                }
            }
            bpNode = bpNode.getPrevious();
        }
        return position;
    }

    @Override
    public Position getLast(IndexEntry outKey, int compareType) {
        IndexEntry key = buildEntry(outKey);
        Position position = root.get(key.getCompareEntry(), compareType);
        if (position == null) {
            return null;
        }
        // 由于存在key一样的情况,所以必须往后遍历,因为前面也可能有相同的key;
        BPNode bpNode = position.getBpNode().getNext();
        while (bpNode != null) {
            boolean notEqualFound = false;
            // 从前往后查找
            for (int i = 0; i < bpNode.entries.size(); i++) {
                IndexEntry item = bpNode.getEntries().get(i);
                if (item.compareIndex(key) == 0) {
                    position.setBpNode(bpNode);
                    position.setPosition(i);
                }
                if (!item.equals(key)) {
                    notEqualFound = true;
                    break;
                }
            }
            if (notEqualFound) {
                break;
            } else {
                bpNode = bpNode.getNext();
            }
        }
        return position;
    }

    // 遍历当前bpNode以及之后的node
    @Override
    public List<IndexEntry> getAll(IndexEntry key) {
        Position res = getFirst(key, CompareType.LOW);
        List<IndexEntry> list = new ArrayList<IndexEntry>();
        BPNode bpNode = res.getBpNode();
        BPNode initNode = res.getBpNode();
        while (bpNode != null) {
            for (IndexEntry indexEntry : bpNode.getEntries()) {
                if (indexEntry.compareIndex(key) == 0) {
                    list.add(indexEntry);
                } else {
                    // 这边对initNode做特殊处理的原因是
                    // 需要将计算出来的firstNode中的等值ke加进来
                    if (initNode != bpNode) {
                        break;
                    }
                }
            }
            bpNode = bpNode.getNext();
        }
        return list;
    }

    public Map<Integer, BPNode> getNodeMap() {
        return nodeMap;
    }

    public BPTree setNodeMap(Map<Integer, BPNode> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }

    public boolean innerRemove(IndexEntry key) {
        return root.remove(key, this);
    }

    @Override
    public int remove(IndexEntry key) {
        int count = 0;
        while (true) {
            if (!innerRemove(key)) {
                break;
            }
            count++;
        }
        return count;
    }

    @Override
    public boolean removeOne(IndexEntry entry) {
        IndexEntry matchIndexEntry = buildEntry(entry);
        return innerRemove(matchIndexEntry);
    }

    @Override
    public void insert(IndexEntry entry, boolean isUnique) {
        IndexEntry matchIndexEntry = buildEntry(entry);
        root.insert(matchIndexEntry, this, isUnique);
    }

    @Override
    public void delete(IndexEntry entry) {
        IndexEntry matchIndexEntry = buildEntry(entry);
        root.remove(matchIndexEntry, this);
    }

    @Override
    public void flushToDisk() {
        writeMetaPage();
        // 深度遍历
        root.flushToDisk(fStore);
    }

    // MetaPage for root page no
    public void writeMetaPage() {
        Page page = PagePool.getIntance().getFreePage();
        page.writeItem(new Item(BpPage.genTupleInt(root.getPageNo())));
        fStore.writePageToFile(page, 0);
    }

    public BPNode getRoot() {
        return root;
    }

    public BPTree setRoot(BPNode root) {
        this.root = root;
        return this;
    }

    public BPNode getHead() {
        return head;
    }

    public BPTree setHead(BPNode head) {
        this.head = head;
        return this;
    }

}
