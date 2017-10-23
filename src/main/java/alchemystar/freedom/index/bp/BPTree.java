package alchemystar.freedom.index.bp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.Relation;
import alchemystar.freedom.meta.Tuple;
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

    public BPTree(Relation relation, String indexName, Attribute[] attributes) {
        super(relation, indexName, attributes);
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
        return ((ValueInt) loader.getTuples()[0].getValues()[0]).getInt();
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
    public GetRes getFirst(Tuple key) {
        GetRes getRes = root.get(key);
        // 由于存在key一样的情况,所以必须往前遍历,因为前面也可能有相同的key;
        BPNode bpNode = getRes.getBpNode().getPrevious();
        while (bpNode != null) {
            // 从后往前倒查找
            for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
                Tuple item = bpNode.getEntries().get(i);
                if (item.compareIndex(key) == 0) {
                    getRes.setBpNode(bpNode);
                    getRes.setTuple(item);
                }
                if (!item.equals(key)) {
                    break;
                }
            }
            bpNode = bpNode.getPrevious();
        }
        return getRes;
    }

    // 遍历当前bpNode以及之后的node
    @Override
    public List<Tuple> getAll(Tuple key) {
        GetRes res = getFirst(key);
        List<Tuple> list = new ArrayList<Tuple>();
        BPNode bpNode = res.getBpNode();
        BPNode initNode = res.getBpNode();
        while (bpNode != null) {
            for (Tuple tuple : bpNode.getEntries()) {
                if (tuple.compareIndex(key) == 0) {
                    list.add(tuple);
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

    public boolean innerRemove(Tuple key) {
        return root.remove(key, this);
    }

    @Override
    public int remove(Tuple key) {
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
    public boolean removeOne(Tuple key) {
        return innerRemove(key);
    }

    @Override
    public void insert(Tuple key, boolean isUnique) {
        root.insert(key, this, isUnique);
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
