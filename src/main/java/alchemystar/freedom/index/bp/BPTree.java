package alchemystar.freedom.index.bp;

import java.util.HashMap;
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
        BpPage bpPage = (BpPage) fStore.readPageFromFile(pageNo, true, this);
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
    public Tuple get(Tuple key) {
        return root.get(key);
    }

    @Override
    public boolean remove(Tuple key) {
        return root.remove(key, this);
    }

    @Override
    public void insert(Tuple key) {
        root.insert(key, this);
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
