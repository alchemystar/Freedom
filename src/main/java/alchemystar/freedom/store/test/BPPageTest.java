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
 * BPPageTest
 *
 * @Author lizhuyang
 */
public class BPPageTest extends AbstractBPTest {

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
        bpTree.flushToDisk();
    }

    @Test
    public void testReadBpIndex() {
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
        System.out.println("read all");
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
        if (str.length() > 40) {
            str = str.substring(0, 40);
        }
        values[1] = new ValueString(str);
        return new Tuple(values);
    }

}
