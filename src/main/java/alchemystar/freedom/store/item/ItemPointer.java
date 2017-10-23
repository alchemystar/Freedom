package alchemystar.freedom.store.item;

import alchemystar.freedom.store.page.Page;

/**
 * ItemPointer
 * Tuple的pointer
 *
 * @Author lizhuyang
 */
public class ItemPointer {

    // Tuple的偏移
    private int offset;
    // Tuple的长度
    private int tupleLength;

    public ItemPointer(int offset, int length) {
        this.offset = offset;
        this.tupleLength = length;
    }

    void write(Page page) {
        page.writeInt(offset);
        page.writeInt(tupleLength);
        // 修改freespace的lowerOffset
        int lowerOffset = page.getLowerOffset();
        lowerOffset += getPtrLength();
        page.modifyLowerOffer(lowerOffset);
    }

    public static int getPtrLength() {
        return 8;
    }

    public int getTupleLength() {
        return tupleLength;
    }

    public int getOffset() {
        return offset;
    }

    public ItemPointer setOffset(int offset) {
        this.offset = offset;
        return this;
    }
}
