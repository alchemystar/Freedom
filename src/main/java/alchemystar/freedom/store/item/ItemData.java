package alchemystar.freedom.store.item;

import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.store.page.Page;

/**
 * ItemData
 * 包装tuple,从而能够和page进行交互
 *
 * @Author lizhuyang
 */
public class ItemData {

    // 帧结构
    // [length]([type][length][data])*
    private IndexEntry indexEntry;
    // Item实际存储的offset
    private int offset;
    // Item实际的长度
    private int length;

    public ItemData(IndexEntry indexEntry) {
        this.indexEntry = indexEntry;
        length = indexEntry.getLength();
    }

    public void write(Page page) {
        // 获取总长度
        int tupleLength = length;
        // 找到写入位置
        int writePosition = page.getUpperOffset() - tupleLength;
        // 写入数据
        page.writeBytes(indexEntry.getBytes(), writePosition);
        // 更新upperOffset
        page.modifyUpperOffset(writePosition);
        // 更新ItemData的offset,length
        offset = writePosition;
    }

    public int getOffset() {
        return offset;
    }

    public ItemData setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public int getLength() {
        return length;
    }

    public ItemData setLength(int length) {
        this.length = length;
        return this;
    }
}
