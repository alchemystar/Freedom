package alchemystar.freedom.index.bp;

import alchemystar.freedom.meta.Tuple;

/**
 * Get请求的返回值
 *
 * @Author lizhuyang
 */
public class Position {

    private BPNode bpNode;

    private int position;

    private Tuple tuple;

    public Position(BPNode bpNode, Tuple tuple) {
        this.bpNode = bpNode;
        this.tuple = tuple;
    }

    public BPNode getBpNode() {
        return bpNode;
    }

    public Position setBpNode(BPNode bpNode) {
        this.bpNode = bpNode;
        return this;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public Position setTuple(Tuple tuple) {
        this.tuple = tuple;
        return this;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
