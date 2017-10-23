package alchemystar.freedom.test;

import java.util.Random;

import org.junit.Test;

import alchemystar.freedom.index.bp.BPNode;
import alchemystar.freedom.index.bp.BPTree;
import alchemystar.freedom.index.bp.GetRes;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueString;

/**
 * @Author lizhuyang
 */
public class BTreeTest {

    @Test
    public void test() {
        BPTree bpTree = new BPTree(null, "bpindex", null);
        int insertSize = 10000;
        for (int i = 1; i <= insertSize; i++) {
            Value[] values = new Value[2];
            // Random random = new Random();
            // int toInsert = random.nextInt(insertSize);
            values[0] = new ValueInt(1);
            values[1] = new ValueString("alchemystar");
            Tuple tuple = new Tuple(values);
            if (i == 9) {
                bpTree.insert(tuple, false);
            } else {
                bpTree.insert(tuple, false);
            }
        }

        for (int i = 2; i <= 10; i++) {
            Value[] values = new Value[2];
            // Random random = new Random();
            // int toInsert = random.nextInt(insertSize);
            values[0] = new ValueInt(i);
            values[1] = new ValueString("alchemystar");
            Tuple tuple = new Tuple(values);
            if (i == 9) {
                bpTree.insert(tuple, false);
            } else {
                bpTree.insert(tuple, false);
            }
        }

       /* Tuple t1 = genTuple(3);
        Tuple t2 = genTuple(5);
        Tuple t3 = genTuple(6);
        Tuple t4 = genTuple(8);
        Tuple t5 = genTuple(9);
        Tuple t6 = genTuple(1);
        Tuple t7 = genTuple(10);
        Tuple t8 = genTuple(4);
        Tuple t9 = genTuple(7);
        Tuple t10 = genTuple(2);
        Tuple t11 = genTuple(11);
        //   Tuple tupleFive = genTuple(5);
        bpTree.remove(t1);
        bpTree.remove(t2);
        bpTree.remove(t3);
        bpTree.remove(t4);
        bpTree.remove(t5);
        //   bpTree.remove(tupleFive);
        bpTree.remove(t6);
        bpTree.remove(t7);
        bpTree.remove(t8);
        bpTree.remove(t9);
        bpTree.remove(t10);
        bpTree.remove(t11);*/
        BPNode node = bpTree.getHead();
        // int sum = 0;
        while (node != null) {
            for (int i = 0; i < node.getEntries().size(); i++) {
                // System.out.println(node.getEntries().getFirst(i));
                Tuple tuple = bpTree.getFirst(node.getEntries().get(i)).getTuple();
                if (tuple == null) {
                    System.out.println("it is null");
                }
            }
            node = node.getNext();
        }

       // printBtree(bpTree.getRoot());
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
                GetRes res = bpTree.getFirst(BPNode.getEntries().get(i));
                Tuple tuple = res.getTuple();
                if (res != null) {
                    bpTree.remove(tuple);
                }
                if (tuple == null) {
                    System.out.println("it is null");
                } else {
                    System.out.println(tuple.getValues()[0]);
                }
            }
            BPNode = BPNode.getPrevious();
        }
        printBtree(bpTree.getRoot());
    }

    @Test
    public void test3() {
        BPTree bpTree = new BPTree(null, "bpindex", null);
        int insertSize = 30000;
        for (int i = 1; i <= insertSize; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            Tuple tuple = genTuple(toInsert);
            bpTree.insert(tuple, true);
        }
        printBtree(bpTree.getRoot());
        for (int i = 1; i <= insertSize * 5; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            Tuple tuple = genTuple(toInsert);
            if (insertSize % 2 == 0) {
                bpTree.remove(tuple);
            } else {
                bpTree.insert(tuple, true);
            }
        }
        BPNode bpNode = bpTree.getHead();

        while (bpNode.getNext() != null) {
            bpNode = bpNode.getNext();
        }
        while (bpNode != null) {
            for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
                GetRes res = bpTree.getFirst(bpNode.getEntries().get(i));
                if (res != null) {
                    bpTree.remove(res.getTuple());
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
