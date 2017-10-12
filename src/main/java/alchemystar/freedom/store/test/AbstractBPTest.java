package alchemystar.freedom.store.test;

import java.util.Random;

import alchemystar.freedom.index.bp.BPNode;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueString;

/**
 * AbstractBPTest
 *
 * @Author lizhuyang
 */
public abstract class AbstractBPTest {

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
