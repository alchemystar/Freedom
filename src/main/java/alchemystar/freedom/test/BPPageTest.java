package alchemystar.freedom.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import alchemystar.freedom.index.bp.BPNode;
import alchemystar.freedom.index.bp.BPTree;
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
        BPTree bpTree = new BPTree(null, "bpindex", null);
        int insertSize = 40000;
        for (int i = 1; i <= insertSize; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            Tuple tuple = genTuple(toInsert);
            bpTree.insert(tuple);
        }
        BPNode node = bpTree.getHead();
        List<Tuple> list = new ArrayList<Tuple>();
        while (node != null) {
            for (int i = 0; i < node.getEntries().size(); i++) {
                list.add(node.getEntries().get(i));
            }
            node = node.getNext();
        }
        for (Tuple item : list) {
            if (!bpTree.remove(item)) {
                System.out.println("remove fail");
            }
        }
        for (int i = 1; i <= insertSize; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            Tuple tuple = genTuple(toInsert);
            if (insertSize % 2 == 0) {
                bpTree.remove(tuple);
            } else {
                bpTree.insert(tuple);
            }
        }
        bpTree.flushToDisk();
    }

    @Test
    public void testReadBpIndex() {
        testWriteBpIndex();
        BPTree bpTree = new BPTree(null, "bpindex", null);
        bpTree.loadFromDisk();
        BPNode bpNode = bpTree.getHead();
        BPNode head = bpNode;
        while (bpNode.getNext() != null) {
            bpNode = bpNode.getNext();
        }
        while (bpNode != null) {
            for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
                Tuple tuple = bpTree.get(bpNode.getEntries().get(i));
                if (tuple == null) {
                    System.out.println("can't find ,error here");
                }
            }
            bpNode = bpNode.getPrevious();
        }
        printBtree(head.getBpTree().getRoot());
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
                            .compare(bpNode.getChildren().get(i).getEntries().get(bpNode.getChildren
                                    ().get(i).getEntries().size() - 1)) <= 0) {
                        throw new RuntimeException("hahaha error");
                    }
                }
                if (i == bpNode.getEntries().size()) {
                    if (bpNode.getEntries().get(i - 1)
                            .compare(bpNode.getChildren().get(i).getEntries().get(bpNode.getChildren
                                    ().get(i).getEntries().size() - 1)) > 0) {
                        throw new RuntimeException("hahaha error");
                    }
                }
                printBtree(bpNode.getChildren().get(i));
            }
        }

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
