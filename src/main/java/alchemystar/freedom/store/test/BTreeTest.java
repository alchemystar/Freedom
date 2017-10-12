package alchemystar.freedom.store.test;

import java.util.Random;

import org.junit.Test;

import alchemystar.freedom.index.bp.BPNode;
import alchemystar.freedom.index.bp.BPTree;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueString;

/**
 * @Author lizhuyang
 */
public class BTreeTest extends BPPageTest {

    @Test
    public void test() {
        BPTree bpTree = new BPTree(null, "bpindex", null);
        int insertSize = 10000;
        for (int i = 1; i <= insertSize; i++) {
            Value[] values = new Value[2];
            // Random random = new Random();
            // int toInsert = random.nextInt(insertSize);
            values[0] = new ValueInt(i);
            values[1] = new ValueString("alchemystar");
            Tuple tuple = new Tuple(values);
            if (i == 9) {
                bpTree.insert(tuple);
            } else {
                bpTree.insert(tuple);
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
        for (int i = 0; i < insertSize; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            Tuple tuple = genTuple(toInsert);
            bpTree.remove(tuple);
        }
      /*  BPNode node = bpTree.getHead();
        // int sum = 0;
        while (node != null) {
            for (int i = 0; i < node.getEntries().size(); i++) {
                // System.out.println(node.getEntries().get(i));
                Tuple tuple = bpTree.get(node.getEntries().get(i));
                if (tuple == null) {
                    System.out.println("it is null");
                }
            }
            node = node.getNext();
        }*/

        printBtree(bpTree.getRoot());
    }

    @Test
    public void test2() {
        BPTree bpTree = new BPTree(null, "bpindex", null);
        bpTree.insert(genTuple(7699));
        bpTree.insert(genTuple(3825));
        bpTree.insert(genTuple(9358));
        bpTree.insert(genTuple(4519));
        bpTree.insert(genTuple(1362));
        bpTree.insert(genTuple(2288));
        bpTree.insert(genTuple(5599));
        bpTree.insert(genTuple(1562));
        bpTree.insert(genTuple(898));
        bpTree.insert(genTuple(9786));
        bpTree.insert(genTuple(9691));
        bpTree.insert(genTuple(4139));
        bpTree.insert(genTuple(9674));
        bpTree.insert(genTuple(3620));
        bpTree.insert(genTuple(5514));
        bpTree.insert(genTuple(6645));
        bpTree.insert(genTuple(6949));
        bpTree.insert(genTuple(8651));
        bpTree.insert(genTuple(9645));
        bpTree.insert(genTuple(5175));
        bpTree.insert(genTuple(6162));
        bpTree.insert(genTuple(6521));
        bpTree.insert(genTuple(3214));
        bpTree.insert(genTuple(7351));
        bpTree.insert(genTuple(7095));
        bpTree.insert(genTuple(3719));
        bpTree.insert(genTuple(1883));
        bpTree.insert(genTuple(1494));
        bpTree.insert(genTuple(9660));
        bpTree.insert(genTuple(1438));
        bpTree.insert(genTuple(6874));
        bpTree.insert(genTuple(2854));
        bpTree.insert(genTuple(5718));
        System.out.println("hahaha");
        BPNode BPNode = bpTree.getHead();

        while (BPNode.getNext() != null) {
            BPNode = BPNode.getNext();
        }
        while (BPNode != null) {
            for (int i = BPNode.getEntries().size() - 1; i >= 0; i--) {
                // System.out.println(BPNode.getEntries().get(i));
                Tuple tuple = bpTree.get(BPNode.getEntries().get(i));
                bpTree.remove(tuple);
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
            bpTree.insert(tuple);
        }
        printBtree(bpTree.getRoot());
        for (int i = 1; i <= insertSize * 5; i++) {
            Random random = new Random();
            int toInsert = random.nextInt(insertSize);
            Tuple tuple = genTuple(toInsert);
            if (insertSize % 2 == 0) {
                bpTree.remove(tuple);
            } else {
                bpTree.insert(tuple);
            }
        }
        BPNode bpNode = bpTree.getHead();

        while (bpNode.getNext() != null) {
            bpNode = bpNode.getNext();
        }
        while (bpNode != null) {
            for (int i = bpNode.getEntries().size() - 1; i >= 0; i--) {
                Tuple tuple = bpTree.get(bpNode.getEntries().get(i));
                bpTree.remove(tuple);
            }
            bpNode = bpNode.getPrevious();
        }
        printBtree(bpTree.getRoot());

    }

}
