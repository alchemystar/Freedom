package alchemystar.freedom.index.bp;

import alchemystar.freedom.meta.IndexEntry;

/**
 * Get请求的返回值
 *
 * @Author lizhuyang
 */
public class Position {

    private BPNode bpNode;

    private int position;

    private IndexEntry searchEntry;

    public Position(BPNode bpNode, int position) {
        this.bpNode = bpNode;
        this.position = position;
    }

    public BPNode getBpNode() {
        return bpNode;
    }

    public Position setBpNode(BPNode bpNode) {
        this.bpNode = bpNode;
        return this;
    }

    public IndexEntry getSearchEntry() {
        return searchEntry;
    }

    public void setSearchEntry(IndexEntry searchEntry) {
        this.searchEntry = searchEntry;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void incrPosition() {
        position++;
    }
}
