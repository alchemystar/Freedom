package alchemystar.freedom.test.bptest;

import java.util.Random;

import org.junit.Test;

import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.index.CompareType;
import alchemystar.freedom.index.bp.BPNode;
import alchemystar.freedom.index.bp.BPTree;
import alchemystar.freedom.index.bp.Position;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueLong;
import alchemystar.freedom.meta.value.ValueString;

/**
 * @Author lizhuyang
 */
public class BTreeTest {

    @Test
    public void clusterIndexTest() {
        BPTree bpTree = new BPTree(getTable(), "bpindex", getClusterAttributes());
        bpTree.setPrimaryKey(true);
        int insertSize = 100345;
        for (int i = 1; i <= insertSize; i++) {
            Value[] values = new Value[2];
            values[0] = new ValueLong(i);
            values[1] = new ValueString("alchemystar" + String.valueOf(i));
            // 主键索引,所以走table attribute
            IndexEntry indexEntry = new IndexEntry(values);
            indexEntry.setIndexDesc(bpTree.getIndexDesc());
            bpTree.insert(indexEntry, false);

        }
        IndexEntry searchEntry = getClusterLowSearchEntry();
        Cursor cursor = bpTree.searchEqual(searchEntry);
        IndexEntry indexEntry;
        while ((indexEntry = cursor.next()) != null) {
            System.out.println(indexEntry);
        }
        cursor = bpTree.searchRange(getClusterLowSearchEntry(), getClusterUpSearchEntry());
        while ((indexEntry = cursor.next()) != null) {
            System.out.println(indexEntry);
        }
        //  printBtree(bpTree.getRoot());
    }

    @Test
    public void secondIndexTest() {
        BPTree bpTree = new BPTree(getTable(), "secondIndex", getSecondAttributes());
        bpTree.setPrimaryKey(false);
        int insertSize = 5000;
        for (int i = 1; i <= insertSize; i++) {
            Value[] values = new Value[2];
            String addPrefix = String.valueOf(i % 1000);
            if (addPrefix.length() == 1) {
                addPrefix = addPrefix + "00";
            } else if (addPrefix.length() == 2) {
                addPrefix = addPrefix + "0";
            }
            values[0] = new ValueString("alchemystar" + addPrefix);
            values[1] = new ValueLong(i);
            // 主键索引,所以走table attribute
            IndexEntry indexEntry = new IndexEntry(values);
            indexEntry.setIndexDesc(bpTree.getIndexDesc());
            bpTree.insert(indexEntry, false);

        }
        IndexEntry searchEntry = getSecondLowSearchEntry();
        Cursor cursor = bpTree.searchEqual(searchEntry);
        IndexEntry indexEntry;
        int count = 0;
        while ((indexEntry = cursor.next()) != null) {
            count++;
            System.out.println(indexEntry);
        }
        System.out.println(count);
        cursor = bpTree.searchRange(getSecondLowSearchEntry(), getSecondUpSearchEntry());
        while ((indexEntry = cursor.next()) != null) {
            System.out.println(indexEntry);
        }
        // printBtree(bpTree.getRoot());
    }

    public IndexEntry getClusterLowSearchEntry() {
        return new IndexEntry(new Value[] {new ValueLong(1345)});
    }

    public IndexEntry getClusterUpSearchEntry() {
        return new IndexEntry(new Value[] {new ValueLong(10000)});
    }

    public IndexEntry getSecondLowSearchEntry() {
        return new IndexEntry(new Value[] {new ValueString("alchemystar200")});
    }

    public IndexEntry getSecondUpSearchEntry() {
        return new IndexEntry(new Value[] {new ValueString("alchemystar510")});
    }

    @Test
    public void test2() {
        BPTree bpTree = new BPTree(null, "bpindex", null);
        bpTree.insert(genTuple(7699), true);
        bpTree.insert(genTuple(3825), true);
        bpTree.insert(genTuple(9358), true);
        bpTree.insert(genTuple(4519), true);
        bpTree.insert(genTuple(1362), true);
        bpTree.insert(genTuple(2288), true);
        bpTree.insert(genTuple(5599), true);
        bpTree.insert(genTuple(1562), true);
        bpTree.insert(genTuple(898), true);
        bpTree.insert(genTuple(9786), true);
        bpTree.insert(genTuple(9691), true);
        bpTree.insert(genTuple(4139), true);
        bpTree.insert(genTuple(9674), true);
        bpTree.insert(genTuple(3620), true);
        bpTree.insert(genTuple(5514), true);
        bpTree.insert(genTuple(6645), true);
        bpTree.insert(genTuple(6949), true);
        bpTree.insert(genTuple(8651), true);
        bpTree.insert(genTuple(9645), true);
        bpTree.insert(genTuple(5175), true);
        bpTree.insert(genTuple(6162), true);
        bpTree.insert(genTuple(6521), true);
        bpTree.insert(genTuple(3214), true);
        bpTree.insert(genTuple(7351), true);
        bpTree.insert(genTuple(7095), true);
        bpTree.insert(genTuple(3719), true);
        bpTree.insert(genTuple(1883), true);
        bpTree.insert(genTuple(1494), true);
        bpTree.insert(genTuple(9660), true);
        bpTree.insert(genTuple(1438), true);
        bpTree.insert(genTuple(6874), true);
        bpTree.insert(genTuple(2854), true);
        bpTree.insert(genTuple(5718), true);
        System.out.println("hahaha");
        BPNode BPNode = bpTree.getHead();

        while (BPNode.getNext() != null) {
            BPNode = BPNode.getNext();
        }
        while (BPNode != null) {
            for (int i = BPNode.getEntries().size() - 1; i >= 0; i--) {
                // System.out.println(BPNode.getEntries().getFirst(i));
                Position res = bpTree.getFirst(BPNode.getEntries().get(i), CompareType.LOW);
                //  IndexEntry indexEntry = res.getIndexEntry();
                //                if (res != null) {
                //                    bpTree.remove(indexEntry);
                //                }
                //                if (indexEntry == null) {
                //                    System.out.println("it is null");
                //                } else {
                //                    System.out.println(indexEntry.getValues()[0]);
                //                }
            }
            BPNode = BPNode.getPrevious();
        }
        printBtree(bpTree.getRoot());
    }

    public Table getTable() {
        Table table = new Table();
        Attribute[] attributes = new Attribute[2];
        attributes[0] = new Attribute("id", 1, 0, "id");
        attributes[1] = new Attribute("name", 2, 1, "name");
        table.setAttributes(attributes);
        return table;
    }

    public Attribute[] getClusterAttributes() {
        Attribute[] attributes = new Attribute[1];
        attributes[0] = new Attribute("id", 1, 0, "id");
        return attributes;
    }

    public Attribute[] getSecondAttributes() {
        Attribute[] attributes = new Attribute[2];
        attributes[0] = new Attribute("name", 1, 0, "name");
        attributes[1] = new Attribute("id", 1, 1, "id");
        return attributes;
    }

    @Test
    public void test3() {
        BPTree bpTree = new BPTree(null, "bpindex", null);
        int insertSize = 30000;
        for (int i = 1; i <= insertSize; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            IndexEntry indexEntry = genTuple(toInsert);
            bpTree.insert(indexEntry, true);
        }
        printBtree(bpTree.getRoot());
        for (int i = 1; i <= insertSize * 5; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            IndexEntry indexEntry = genTuple(toInsert);
            if (insertSize % 2 == 0) {
                bpTree.remove(indexEntry);
            } else {
                bpTree.insert(indexEntry, true);
            }
        }
        BPNode bpNode = bpTree.getHead();

        while (bpNode.getNext() != null) {
            bpNode = bpNode.getNext();
        }
        while (bpNode != null) {
            for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
                Position res = bpTree.getFirst(bpNode.getEntries().get(i), CompareType.LOW);
                if (res != null) {
                    //                    bpTree.remove(res.getIndexEntry());
                }
            }
            bpNode = bpNode.getPrevious();
        }
        printBtree(bpTree.getRoot());

    }

    public static void printBtree(BPNode bpNode) {
        if (bpNode == null) {
            return;
        }

        if ((!bpNode.isLeaf()) && ((bpNode.getEntries().size() + 1) != bpNode.getChildren().size())) {
            System.out.println("B+Tree Error");
        }

        double spaceRate = bpNode.getBpPage().getContentSize() * 1.0 / bpNode.getBpPage().getInitFreeSpace();
        System.out.println("node space rate=" + spaceRate);

        if (!bpNode.isLeaf()) {
            for (int i = 0; i < bpNode.getChildren().size(); i++) {
                if (bpNode.getChildren().get(i).getParent() != bpNode) {
                    System.out.println("parent BPNode error");
                    throw new RuntimeException("error");
                }
                if (bpNode.getEntries().size() + 1 != bpNode.getChildren().size()) {
                    throw new RuntimeException("cacaca error");
                }
                if (i < bpNode.getEntries().size()) {
                    if (bpNode.getEntries().get(i)
                            .compareIndex(bpNode.getChildren().get(i).getEntries().get(bpNode.getChildren
                                    ().get(i).getEntries().size() - 1)) <= 0) {
                        throw new RuntimeException("hahaha error");
                    }
                }
                if (i == bpNode.getEntries().size()) {
                    if (bpNode.getEntries().get(i - 1)
                            .compareIndex(bpNode.getChildren().get(i).getEntries().get(bpNode.getChildren
                                    ().get(i).getEntries().size() - 1)) > 0) {
                        throw new RuntimeException("hahaha error");
                    }
                }
                printBtree(bpNode.getChildren().get(i));
            }
        }

    }

    public static IndexEntry genTuple(int i) {
        Value[] values = new Value[2];
        values[0] = new ValueInt(i);
        Random random = new Random();
        int strSize = random.nextInt(20) + 1;
        String str = "";
        for (int j = 0; j < strSize; j++) {
            str = str + random.nextInt();
        }
        if (str.length() > 80) {
            str = str.substring(0, 80);
        }
        values[1] = new ValueString(str);
        return new IndexEntry(values);
    }

}
