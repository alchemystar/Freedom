package alchemystar.freedom.index.bp;

import alchemystar.freedom.meta.Tuple;

/**
 * Get请求的返回值
 *
 * @Author lizhuyang
 */
public class GetRes {

    private BPNode bpNode;

    private Tuple tuple;

    public GetRes(BPNode bpNode, Tuple tuple) {
        this.bpNode = bpNode;
        this.tuple = tuple;
    }

    public BPNode getBpNode() {
        return bpNode;
    }

    public GetRes setBpNode(BPNode bpNode) {
        this.bpNode = bpNode;
        return this;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public GetRes setTuple(Tuple tuple) {
        this.tuple = tuple;
        return this;
    }
}
