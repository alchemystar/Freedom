package alchemystar.freedom.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import alchemystar.freedom.index.bp.BPNode;
import alchemystar.freedom.index.bp.BPTree;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueString;

/**
 * BPPageTest
 *
 * @Author lizhuyang
 */
public class BPPageTest {

    @Test
    public void testWriteBpIndex() {
        BPTree bpTree = new BPTree(null, "bpindex", getAttr());
        int insertSize = 40000;
        for (int i = 1; i <= insertSize; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            Tuple tuple = genTuple(toInsert);
            bpTree.insert(tuple, false);
        }
        BPNode node = bpTree.getHead();
        List<Tuple> list = new ArrayList<Tuple>();
        Tuple prev1 = null;
        //        while (node != null) {
        //            for (int i = 0; i < node.getEntries().size(); i++) {
        //                // 去重处理
        //                if (prev1 != null && prev1.compareIndex(node.getEntries().get(i)) != 0) {
        //                    list.add(node.getEntries().get(i));
        //                }
        //                prev1 = node.getEntries().get(i);
        //            }
        //            node = node.getNext();
        //        }
        //        for (Tuple item : list) {
        //            if (bpTree.remove(item) == 0) {
        //                System.out.println("prev item=" + item);
        //                System.out.println("current item=" + item);
        //                bpTree.remove(item);
        //            }
        //        }
        for (int i = 1; i <= insertSize; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            Tuple tuple = genTuple(toInsert);
            if (insertSize % 2 == 0) {
                bpTree.remove(tuple);
            } else {
                bpTree.insert(tuple, false);
            }
        }
        bpTree.flushToDisk();
    }

    @Test
    public void testReadBpIndex() {
        testWriteBpIndex();
        BPTree bpTree = new BPTree(null, "bpindex", getAttr());
        bpTree.loadFromDisk();
        BPNode bpNode = bpTree.getHead();
        BPNode head = bpNode;
        while (bpNode.getNext() != null) {
            bpNode = bpNode.getNext();
        }
        while (bpNode != null) {
            for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
                List<Tuple> res = bpTree.getAll(bpNode.getEntries().get(i));
                if (res.size() == 0) {
                    System.out.println("error can't find");
                }
                break;
            }
            bpNode = bpNode.getPrevious();
        }
        // printBtree(head.getBpTree().getRoot());
    }

    @Test
    public void multiTest() {
        for (int i = 0; i < 10; i++) {
            testReadBpIndex();
        }
    }

    public static void printBtree(BPNode bpNode) {
        if (bpNode == null) {
            return;
        }

        if ((!bpNode.isLeaf()) && ((bpNode.getEntries().size() + 1) != bpNode.getChildren().size())) {
            System.out.println("B+Tree Error");
        }

        double spaceRate = bpNode.getBpPage().getContentSize() * 1.0 / bpNode.getBpPage().getInitFreeSpace();
        //  System.out.println("node space rate=" + spaceRate);

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
                                    ().get(i).getEntries().size() - 1)) >= 0) {
                        throw new RuntimeException("hahaha error");
                    }
                }
                printBtree(bpNode.getChildren().get(i));
            }
        }

    }

    public static Attribute[] getAttr() {
        Attribute attr1 = new Attribute("id", Value.INT, 0, "id");
        Attribute attr2 = new Attribute("name", Value.STRING, 0, "name");
        Attribute[] attributes = new Attribute[2];
        attributes[0] = attr1;
        attributes[1] = attr2;
        return attributes;
    }

    public static Tuple genTuple(int i) {
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
        return new Tuple(values);
    }
}
