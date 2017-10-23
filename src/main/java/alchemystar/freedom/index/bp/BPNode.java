package alchemystar.freedom.index.bp;

import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.constant.ItemConst;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.store.fs.FStore;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.store.page.PageFactory;

/**
 * BPNode
 * todo 节点内部查询用二分法
 * 必须限定key的size < InitFreeSize/3
 *
 * @Author lizhuyang
 */
public class BPNode {
    /**
     * 是否为叶子节点
     */
    protected boolean isLeaf;

    /**
     * 是否为根节点
     */
    protected boolean isRoot;
    /**
     * 对应的pageNo
     */
    protected int pageNo;
    /**
     * 父节点
     */
    protected BPNode parent;

    /**
     * 叶节点的前节点
     */
    protected BPNode previous;

    /**
     * 叶节点的后节点
     */
    protected BPNode next;

    /**
     * 节点的关键字
     */
    protected List<Tuple> entries;

    /**
     * 子节点
     */
    protected List<BPNode> children;

    /**
     * 页结构
     */
    protected BpPage bpPage;

    /**
     * 隶属于哪个bpTree
     */
    protected BPTree bpTree;

    public BPNode(boolean isLeaf, BPTree bpTree) {
        this.isLeaf = isLeaf;
        this.bpTree = bpTree;
        this.pageNo = bpTree.getNextPageNo();
        // 默认root是false;
        entries = new ArrayList<Tuple>();
        if (!isLeaf) {
            children = new ArrayList<BPNode>();
        }
        bpPage = PageFactory.getInstance().newBpPage(this);
    }

    public BPNode(boolean isLeaf, boolean isRoot, BPTree bpTree) {
        this(isLeaf, bpTree);
        this.isRoot = isRoot;
    }

    public GetRes get(Tuple key) {
        if (isLeaf) {
            for (Tuple tuple : entries) {
                if (tuple.compareIndex(key) == 0) {
                    return new GetRes(this, tuple);
                }
            }
            return null;
        } else {
            // 非叶子节点
            // 如果key<最左边的key,沿第一个子节点继续搜索
            if (key.compareIndex(entries.get(0)) < 0) {
                return children.get(0).get(key);
            } else if (key.compareIndex(entries.get(entries.size() - 1)) >= 0) {
                // 如果key >  最右边的key,则按照最后一个子节点搜索
                return children.get(children.size() - 1).get(key);
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    // 比key大的前一个子节点继续搜索
                    if (key.compareIndex(entries.get(i)) >= 0 && key.compareIndex(entries.get(i + 1)) < 0) {
                        return children.get(i + 1).get(key);
                    }
                }
            }
        }
        return null;
    }

    public void insert(Tuple key, BPTree tree, boolean isUnique) {
        if (getBorrowKeyLength(key) > bpPage.getInitFreeSpace() / 3) {
            throw new RuntimeException("key size must <= Max/3");
        }
        if (isLeaf) {
            // 无需分裂
            if (!isLeafSplit(key)) {
                innerInsert(key, isUnique);
            } else {
                // 需要分裂
                // 则分裂成左右两个节点
                BPNode left = new BPNode(true, bpTree);
                BPNode right = new BPNode(true, bpTree);
                if (previous != null) {
                    previous.setNext(left);
                    left.setPrevious(previous);
                }
                if (next != null) {
                    // 相当于在list中插入了一个数据
                    next.setPrevious(right);
                    right.setNext(next);
                }
                if (previous == null) {
                    tree.setHead(left);
                }
                left.setNext(right);
                right.setPrevious(left);
                previous = null;
                next = null;
                // 插入后再分裂
                innerInsert(key, isUnique);
                int leftSize = this.entries.size() / 2;
                int rightSize = this.entries.size() - leftSize;

                // 左右节点,分别赋值
                for (int i = 0; i < leftSize; i++) {
                    left.getEntries().add(entries.get(i));
                }
                // 叶子节点需要全拷贝
                for (int i = 0; i < rightSize; i++) {
                    right.getEntries().add(entries.get(leftSize + i));
                }

                // 表明当前节点不是根节点
                if (parent != null) {
                    // 调整父子节点的关系
                    // 寻找到当前节点对应的index
                    int index = parent.getChildren().indexOf(this);
                    // 删掉当前节点
                    parent.getChildren().remove(this);
                    left.setParent(parent);
                    right.setParent(parent);
                    // 将节点增加到parent上面
                    parent.getChildren().add(index, left);
                    parent.getChildren().add(index + 1, right);
                    // 回收
                    recycle();
                    // 插入关键字
                    parent.innerInsert(right.getEntries().get(0), isUnique);
                    // 更新
                    parent.updateInsert(tree);
                    setParent(null);
                } else {
                    // 如果是根节点
                    isRoot = false;
                    // 根节点的分裂
                    BPNode parent = new BPNode(false, true, bpTree);
                    tree.setRoot(parent);
                    left.setParent(parent);
                    right.setParent(parent);
                    parent.getChildren().add(left);
                    parent.getChildren().add(right);
                    // 回收
                    recycle();
                    // 插入关键字
                    parent.innerInsert(right.getEntries().get(0), isUnique);
                    // 更新根节点
                    parent.updateInsert(tree);
                }
            }
        } else {
            // 如果不是叶子节点,沿着第一个子节点继续搜索
            if (key.compareIndex(entries.get(0)) < 0) {
                children.get(0).insert(key, tree, isUnique);
            } else if (key.compareIndex(entries.get(entries.size() - 1)) >= 0) {
                // 沿最后一个子节点继续搜索
                children.get(children.size() - 1).insert(key, tree, isUnique);
            } else {
                //否则沿比key大的前一个子节点继续搜索
                for (int i = 0; i < entries.size(); i++) {
                    // 比key大的前一个子节点继续搜索
                    if (key.compareIndex(entries.get(i)) >= 0 && key.compareIndex(entries.get(i + 1)) < 0) {
                        children.get(i + 1).insert(key, tree, isUnique);
                        break;
                    }
                }
            }
        }
    }

    protected boolean remove(Tuple key, BPTree tree) {
        boolean found = false;
        // 如果是叶子节点
        if (isLeaf) {
            // 如果不包含此key,则直接返回
            if (!leafContains(key)) {
                return false;
            }
            found = true;
            // 叶子节点 && 根节点,表明只有此一个节点,直接删除
            if (isRoot) {
                remove(key);
            } else {
                if (canRemoveDirect(key)) {
                    remove(key);
                } else {
                    // 先删除key
                    remove(key);
                    // 如果自身关键字数小于M/2,并且前节点关键字数大于M/2,则从其处借补
                    if (canLeafBorrowPrevious()) {
                        borrowLeafPrevious();
                    } else if (canLeafBorrowNext()) {
                        borrowLeafNext();
                    } else {
                        // 现在需要合并叶子节点
                        if (canLeafMerge(previous)) {
                            // 与前节点合并
                            addPreNode(previous);
                            previous.recycle();
                            // 删掉当前节点的entry
                            int currEntryIndex = getParentEntry(this);
                            if (parent == null || parent.getEntries() == null || currEntryIndex < 0) {
                                currEntryIndex = getParentEntry(this);
                            }
                            parent.getEntries().remove(currEntryIndex);
                            // 然后删掉前驱
                            parent.getChildren().remove(previous);
                            previous.setParent(null);
                            // 更新链表
                            if (previous.getPrevious() != null) {
                                BPNode temp = previous;
                                temp.getPrevious().setNext(this);
                                // 更新前驱
                                previous = temp.getPrevious();
                                temp.setPrevious(null);
                                temp.setNext(null);
                            } else {
                                tree.setHead(this);
                                previous.setNext(null);
                                previous = null;
                            }
                        } else if (canLeafMerge(next)) {
                            addNextNode(next);
                            next.recycle();
                            // 同时删掉后继节点的entry
                            int currEntryIndex = getParentEntry(this.next);
                            parent.getEntries().remove(currEntryIndex);
                            parent.getChildren().remove(next);
                            // 更新链表
                            if (next.getNext() != null) {
                                BPNode temp = next;
                                temp.getNext().setPrevious(this);
                                next = temp.getNext();
                                temp.setPrevious(null);
                                temp.setNext(null);
                            } else {
                                next.setPrevious(null);
                                next = null;
                            }
                        }
                    }
                }
                parent.updateRemove(tree);
            }
        } else {
            // 如果不是叶子节点,沿着第一个子节点继续搜索
            if (key.compareIndex(entries.get(0)) < 0) {
                if (children.get(0).remove(key, tree)) {
                    found = true;
                }
            } else if (key.compareIndex(entries.get(entries.size() - 1)) >= 0) {
                // 沿最后一个子节点继续搜索
                if (children.get(children.size() - 1).remove(key, tree)) {
                    found = true;
                }
            } else {
                //否则沿比key大的前一个子节点继续搜索
                for (int i = 0; i < entries.size(); i++) {
                    if (key.compareIndex(entries.get(i)) >= 0 && key.compareIndex(entries.get(i + 1)) < 0) {
                        if (children.get(i + 1).remove(key, tree)) {
                            found = true;
                        }
                        break;
                    }
                }
            }
        }

        return found;
    }

    // 删除节点后的中间节点更新
    protected void updateRemove(BPTree tree) {
        if (children.size() < 2 || bpPage.getContentSize() < bpPage.getInitFreeSpace() / 2) {
            if (isRoot) {
                // 根节点并且子节点树>=2 , 直接return
                if (children.size() >= 2) {
                    return;
                } else {
                    // 如果 < 2,则需要和子节点合并
                    // 直接将子节点做为根节点
                    // 由于此会导致根节点的pageNo不为0
                    BPNode root = children.get(0);
                    tree.setRoot(root);
                    root.setParent(null);
                    root.setRoot(true);
                    // recyle废弃的根节点
                    recycle();
                }
            } else {
                // 计算前后节点
                int currIdx = parent.getChildren().indexOf(this);
                int prevIdx = currIdx - 1;
                int nextIdx = currIdx + 1;
                BPNode previous = null, next = null;
                if (prevIdx >= 0) {
                    previous = parent.getChildren().get(prevIdx);
                }
                if (nextIdx < parent.getChildren().size()) {
                    next = parent.getChildren().get(nextIdx);
                }
                if (canNodeBorrowPrevious(previous)) {
                    // 从前驱处借
                    // 从前叶子节点末尾节点添加到首位
                    borrowNodePrevious(previous);
                } else if (canNodeBorrowNext(next)) {
                    // 从后继中借
                    borrowNodeNext(next);
                } else {
                    // 现在需要合并子节点
                    if (canMergePrevois(previous)) {
                        // 与前节点合并
                        addPreNode(previous);
                        previous.recycle();
                        // 删掉当前节点的entry
                        int currEntryIndex = getParentEntry(this);
                        parent.getEntries().remove(currEntryIndex);
                        // 删掉前驱
                        parent.getChildren().remove(previous);
                    } else if (canMergeNext(next)) {
                        // 与后节点合并
                        addNextNode(next);
                        next.recycle();
                        // 同时删掉后继节点的entry
                        int currEntryIndex = getParentEntry(next);
                        parent.getEntries().remove(currEntryIndex);
                        // 删掉后继
                        parent.getChildren().remove(next);
                    }
                }
                // 这边不会出现entries是0的情况,如果entries是0,会在前面的borrow节点给borrow掉,不会到达这里
                parent.updateRemove(tree);
            }
        } else if (this.bpPage.getContentSize() > this.bpPage.getInitFreeSpace()) {
            // now this way show split
            // 因为在更新的时候,由于key值大小不定,可能导致虽然删除了关键字,但是由于
            // 更新了新的长的key,导致比删除之前的size还要大,所以就有可能导致分裂
            // 即changeKeySize - deleteKeySize > 0的某些情况下会导致分裂
            updateInsert(bpTree);
        }
    }

    private int getParentEntry(BPNode BPNode) {
        int index = parent.getChildren().indexOf(BPNode);
        return index - 1;
    }

    public void addPreNode(BPNode bpNode) {
        if (!bpNode.isLeaf()) {
            int parentIdx = this.getParentEntry(this);
            // 事实上是父parent的entry下移
            entries.add(0, this.getParent().getEntries().get(parentIdx));
        }
        for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
            entries.add(0, bpNode.getEntries().get(i));
        }
        if (!bpNode.isLeaf()) {
            for (int i = bpNode.getChildren().size() - 1; i >= 0; i--) {
                bpNode.getChildren().get(i).setParent(this);
                children.add(0, bpNode.getChildren().get(i));
            }
        }
    }

    public void addNextNode(BPNode bpNode) {
        // 后驱节点的entry下移
        // 叶子节点无需下移
        if (!bpNode.isLeaf()) {
            int parentIdx = this.getParentEntry(bpNode);
            entries.add(bpNode.getParent().getEntries().get(parentIdx));
        }

        for (int i = 0; i < bpNode.getEntries().size(); i++) {
            entries.add(bpNode.getEntries().get(i));
        }
        if (bpNode.getChildren() != null) {
            for (int i = 0; i < bpNode.getChildren().size(); i++) {
                bpNode.getChildren().get(i).setParent(this);
                children.add(bpNode.getChildren().get(i));
            }
        }
    }

    /**
     * 插入到当前节点的关键字中
     * 有序
     */
    protected void innerInsert(Tuple tuple, boolean isUnique) {
        //如果关键字列表长度为0，则直接插入
        if (entries.size() == 0) {
            entries.add(tuple);
            return;
        }
        //否则遍历列表
        for (int i = 0; i < entries.size(); i++) {
            //如果该关键字键值已存在，则更新
            if (entries.get(i).compareIndex(tuple) == 0) {
                if (isUnique) {
                    throw new RuntimeException("Duplicated Key error");
                }
                // 如果相等的话,放在相等集合中的任何一个插入点都是可以的
                entries.add(i, tuple);
                return;
                //否则插入
            } else if (entries.get(i).compareIndex(tuple) > 0) {
                //插入到链首
                if (i == 0) {
                    entries.add(0, tuple);
                    return;
                    //插入到中间
                } else {
                    entries.add(i, tuple);
                    return;
                }
            }
        }
        //插入到末尾
        entries.add(entries.size(), tuple);
    }

    private void borrowNodePrevious(BPNode previous) {
        int size = previous.getEntries().size();
        int childSize = previous.getChildren().size();
        // 将previous的最后一个entry到parent对应index的下一个指向,再将父节点对应的entry下放到当前节点
        //      10
        // 3 9       11
        // 变换为
        //      9
        // 3          10
        int parentIdx = getParentEntry(previous) + 1;
        // 先下放
        Tuple downerKey = parent.getEntries().get(parentIdx);
        // 由于是向previous借,肯定是0
        entries.add(0, downerKey);
        // previous的上提
        parent.getEntries().remove(parentIdx);
        parent.getEntries().add(parentIdx, previous.getEntries().get(size - 1));
        previous.getEntries().remove(size - 1);
        // 将child也借过来
        BPNode borrowChild = previous.getChildren().get(childSize - 1);
        children.add(0, borrowChild);
        borrowChild.setParent(this);
        previous.getChildren().remove(borrowChild);
    }

    private void borrowNodeNext(BPNode next) {
        // 将next的第一个entry上提,再将父节点对应的entry下放到当前节点
        //      10
        // 3       11  12
        // 变换为
        //      11
        // 10         12
        // 将child也借过来
        int parentIdx = getParentEntry(next);
        // 先下放
        Tuple downerKey = parent.getEntries().get(parentIdx);
        // 由于是向next借,所以肯定是最后
        entries.add(downerKey);
        // next的上提
        parent.getEntries().remove(parentIdx);
        parent.getEntries().add(parentIdx, next.getEntries().get(0));
        next.getEntries().remove(0);

        // 将child也借过来
        BPNode borrowChild = next.getChildren().get(0);
        children.add(borrowChild);
        borrowChild.setParent(this);
        next.getChildren().remove(borrowChild);
    }

    private void borrowLeafPrevious() {
        int size = previous.getEntries().size();
        // 从previous借最后一个过来,加到当前entry最前面
        Tuple borrowKey = previous.getEntries().get(size - 1);
        previous.getEntries().remove(borrowKey);
        entries.add(0, borrowKey);
        // 找到当前节点在父节点中的entries
        int currEntryIdx = getParentEntry(this);
        parent.getEntries().remove(currEntryIdx);
        parent.getEntries().add(currEntryIdx, borrowKey);
    }

    private void borrowLeafNext() {
        // 从next借第一个过来,加到当前entry最后面
        Tuple borrowKey = next.getEntries().get(0);
        next.getEntries().remove(borrowKey);
        entries.add(borrowKey);
        // 找到当前节点的后继节点在父节点中的parent
        int currEntryIdx = getParentEntry(this.next);
        parent.getEntries().remove(currEntryIdx);
        // 是将next的第一个上提,而不是borrowKey
        // 由于之前remove过了,所以index还是0
        parent.getEntries().add(currEntryIdx, next.getEntries().get(0));
    }

    protected void updateInsert(BPTree tree) {
        // 当前页面存不下,需要分裂
        if (isNodeSplit()) {
            BPNode left = new BPNode(false, bpTree);
            BPNode right = new BPNode(false, bpTree);
            int leftSize = this.entries.size() / 2;
            int rightSize = this.entries.size() - leftSize;
            // 左边复制entry
            for (int i = 0; i < leftSize; i++) {
                left.getEntries().add(entries.get(i));
            }
            // 左边复制child
            for (int i = 0; i <= leftSize; i++) {
                left.getChildren().add(children.get(i));
                children.get(i).setParent(left);
            }
            // 右边的第一个关键字上提到父节点,当前entries不保存
            // 右边复制entries
            for (int i = 1; i < rightSize; i++) {
                right.getEntries().add(entries.get(leftSize + i));
            }
            // 右边复制child
            for (int i = 1; i < rightSize + 1; i++) {
                right.getChildren().add(children.get(leftSize + i));
                children.get(leftSize + i).setParent(right);
            }
            Tuple keyToUpdateParent = entries.get(leftSize);
            if (parent != null) {
                int index = parent.getChildren().indexOf(this);
                parent.getChildren().remove(this);
                left.setParent(parent);
                right.setParent(parent);
                parent.getChildren().add(index, left);
                parent.getChildren().add(index + 1, right);
                // 插入关键字
                parent.innerInsert(keyToUpdateParent, false);
                // 父节点更新关键字
                parent.updateInsert(tree);
                recycle();
            } else {
                // 如果是根节点
                isRoot = false;
                BPNode parent = new BPNode(false, true, bpTree);
                tree.setRoot(parent);
                left.setParent(parent);
                right.setParent(parent);
                parent.getChildren().add(left);
                parent.getChildren().add(right);
                recycle();
                // 插入关键字
                parent.innerInsert(keyToUpdateParent, false);
                // 更新根节点
                parent.updateInsert(tree);
            }
        }

    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public BPNode setLeaf(boolean leaf) {
        isLeaf = leaf;
        return this;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public BPNode setRoot(boolean root) {
        isRoot = root;
        return this;
    }

    public BPNode getParent() {
        return parent;
    }

    public BPNode setParent(BPNode parent) {
        this.parent = parent;
        return this;
    }

    public BPNode getPrevious() {
        return previous;
    }

    public BPNode setPrevious(BPNode previous) {
        this.previous = previous;
        return this;
    }

    public BPNode getNext() {
        return next;
    }

    public BPNode setNext(BPNode next) {
        this.next = next;
        return this;
    }

    public List<Tuple> getEntries() {
        return entries;
    }

    public BPNode setEntries(List<Tuple> entries) {
        this.entries = entries;
        return this;
    }

    public List<BPNode> getChildren() {
        return children;
    }

    public BPNode setChildren(List<BPNode> children) {
        this.children = children;
        return this;
    }

    // 判断当前节点是否包含此tuple
    protected boolean leafContains(Tuple tuple) {
        // 这边由于
        for (Tuple item : entries) {
            if (item.compare(tuple) == 0) {
                return true;
            }
        }
        return false;
    }

    protected int getRemoveKeyIndex(Tuple key) {
        int index = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).compareIndex(key) == 0) {
                index = i;
                return index;
            }
        }
        return -1;
    }

    // 删除节点
    protected boolean remove(Tuple key) {
        int index = -1;
        boolean foud = false;
        for (int i = 0; i < entries.size(); i++) {
            // 由于是leaf,所以要比较所有的key
            if (entries.get(i).compare(key) == 0) {
                index = i;
                foud = true;
                break;
            }
        }
        if (index != -1) {
            entries.remove(index);
        }
        if (foud) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isLeafSplit(Tuple key) {
        if (bpPage.cacluateRemainFreeSpace() < Item.getItemLength(key)) {
            return true;
        }
        return false;
    }

    public boolean isNodeSplit() {
        if (bpPage.cacluateRemainFreeSpace() < 0) {
            return true;
        }
        return false;
    }

    public boolean canRemoveDirect(Tuple key) {
        if ((bpPage.getContentSize() - Item.getItemLength(key)) > bpPage.getInitFreeSpace() / 2) {
            return true;
        }
        return false;
    }

    public boolean canLeafBorrowPrevious() {
        if (previous != null && previous.getEntries().size() > 2 && previous.getParent() == parent) {
            Tuple borrowKey = previous.getEntries().get(previous.getEntries().size() - 1);
            int borrowKeyLength = getBorrowKeyLength(borrowKey);
            if ((previous.bpPage.getContentSize() - borrowKeyLength > previous.bpPage.getInitFreeSpace() / 2)) {
                return true;
            }
            // 即将删除到0,所以需要borrow
            if (this.entries.size() == 1 && previous.getEntries().size() >= 2) {
                return true;
            }
        }
        return false;
    }

    public boolean canLeafBorrowNext() {
        if (next != null && next.getEntries().size() > 2 && next.getParent() == parent) {
            Tuple borrowKey = next.getEntries().get(0);
            int borrowKeyLength = getBorrowKeyLength(borrowKey);
            if ((next.bpPage.getContentSize() - borrowKeyLength > next.bpPage.getInitFreeSpace() / 2)) {
                return true;
            }
            // 即将删除到0,所以需要borrow
            if (this.entries.size() == 1 && next.getEntries().size() >= 2) {
                return true;
            }
        }
        return false;
    }

    public boolean canNodeBorrowPrevious(BPNode bpNode) {
        if (bpNode == null || bpNode.getEntries().size() < 2) {
            return false;
        }
        return canNodeBorrow(bpNode, bpNode.getEntries().get(bpNode.getEntries().size() - 1));
    }

    public boolean canNodeBorrowNext(BPNode bpNode) {
        if (bpNode == null || bpNode.getEntries().size() < 2) {
            return false;
        }
        return canNodeBorrow(bpNode, bpNode.getEntries().get(0));
    }

    public boolean canNodeBorrow(BPNode bpNode, Tuple key) {
        if (bpNode == null) {
            return false;
        }
        // borrowKey的时候,还需要加child也一并borrow
        int borrowKeyLength = getBorrowKeyLength(key);
        if (bpNode.getParent() == parent &&
                bpNode.getEntries().size() >= 2 &&
                bpNode.bpPage.getContentSize() - borrowKeyLength > bpNode.bpPage.getInitFreeSpace() / 2 &&
                borrowKeyLength <= this.bpPage.cacluateRemainFreeSpace()) {
            return true;
        } else if (this.entries.size() == 0 && bpNode.getEntries().size() >= 2) {
            // 这边特殊处理,是为了不破坏B+树的性质
            // 这里不检查borrowKeyLength <= remainFreeSpace的原因是
            // 限定了key <= 3/Max,而,之前的非直接删除条件是,contentSize <= Max/2
            // 所以数学上 contentSize + key <=5/6 Max,不会溢出
            return true;
        } else {
            return false;
        }
    }

    public boolean canLeafMerge(BPNode bpNode) {
        if (bpNode == null) {
            return false;
        }
        // 加上contentSize<space/2这个条件,是为了防止频繁的合并分裂节点
        if (bpNode != null && bpNode.bpPage.getContentSize() < bpNode.bpPage.getInitFreeSpace() / 2 && bpNode.bpPage
                .getContentSize() <= bpPage.cacluateRemainFreeSpace()
                && bpNode.getParent() == parent) {
            return true;
        }
        return false;
    }

    public boolean canMergePrevois(BPNode bpNode) {
        if (bpNode == null) {
            return false;
        }
        if (bpNode != null && bpNode.getParent() == parent) {
            int adjutSize = 0;
            if (!bpNode.isLeaf()) {
                int parentIdx = this.getParentEntry(this);
                // 事实上是父parent的entry下移
                Tuple downKey = this.getParent().getEntries().get(parentIdx);
                adjutSize = Item.getItemLength(downKey);
            }
            if (bpNode.bpPage.getContentSize() + adjutSize <= bpPage.cacluateRemainFreeSpace()) {
                return true;
            }
        }
        return false;
    }

    public boolean canMergeNext(BPNode bpNode) {
        if (bpNode == null) {
            return false;
        }
        if (bpNode != null && bpNode.getParent() == parent) {
            int adjutSize = 0;
            if (!bpNode.isLeaf()) {
                int parentIdx = this.getParentEntry(bpNode);
                // 事实上是父parent的entry下移
                Tuple downKey = this.getParent().getEntries().get(parentIdx);
                adjutSize = Item.getItemLength(downKey);
            }
            if (bpNode.bpPage.getContentSize() + adjutSize <= bpPage.cacluateRemainFreeSpace()) {
                return true;
            }
        }
        return false;
    }

    public int getPageNo() {
        return pageNo;
    }

    public BPNode setPageNo(int pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public BpPage getBpPage() {
        return bpPage;
    }

    public BPNode setBpPage(BpPage bpPage) {
        this.bpPage = bpPage;
        return this;
    }

    private int getBorrowKeyLength(Tuple key) {
        // 因为borrowKey的话,需要将child也borrow过来
        if (!isLeaf) {
            return Item.getItemLength(key) + ItemConst.INT_LEANGTH;
        } else {
            return Item.getItemLength(key);
        }
    }

    private void recycle() {
        setEntries(null);
        setChildren(null);
        bpTree.recyclePageNo(pageNo);
    }

    public void flushToDisk(FStore fStore) {
        bpPage.writeToPage();
        fStore.writePageToFile(bpPage, pageNo);
        if (isLeaf) {
            return;
        }
        for (BPNode bpNode : children) {
            bpNode.flushToDisk(fStore);
        }
    }

    public BPTree getBpTree() {
        return bpTree;
    }

    public BPNode setBpTree(BPTree bpTree) {
        this.bpTree = bpTree;
        return this;
    }
}


