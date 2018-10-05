package alchemystar.freedom.store.page;

import java.util.List;

import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.store.item.ItemPointer;
import alchemystar.freedom.util.BufferWrapper;

/**
 * Page Model
 *
 * @Author lizhuyang
 */
public class Page {

    protected PageHeaderData pageHeaderData;
    protected BufferWrapper bufferWrapper;
    protected int length;
    // 是否是脏页,如果是脏页的话,需要写入
    protected boolean dirty;

    public Page(int defaultSize) {
        pageHeaderData = new PageHeaderData(defaultSize);
        bufferWrapper = new BufferWrapper(new byte[defaultSize]);
        length = defaultSize;
        // 首先write pageHeaderData;
        pageHeaderData.write(this);
        dirty = false;
    }

    public void read(byte[] buffer) {
        bufferWrapper = new BufferWrapper(buffer);
    }

    public void addTupleCount(Page page) {
        pageHeaderData.addTupleCount(page);
    }

    public void decTupleCount(Page page) {
        pageHeaderData.decTupleCount(page);
    }

    public boolean spaceEnough(IndexEntry indexEntry) {
        Item item = new Item(indexEntry);
        if (remainFreeSpace() < item.getLength()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean writeTuple(IndexEntry indexEntry) {
        return writeItem(new Item(indexEntry));
    }

    public boolean writeItem(Item item) {
        if (item.writeItem(this)) {
            // 表明此页已脏
            dirty = true;
            return true;
        } else {
            return false;
        }
    }

    public void delete(int pageCount) {
        // 其实质是将对应的itemLength设置为-1
        int position = pageHeaderData.getLength() + ItemPointer.getPtrLength() * pageCount + 4;
        writeIntPos(-1, position);
        // 由于page本身存储的item count不变,所以不需要变动item count
        dirty = true;
    }

    public void writeItems(List<Item> items) {
        int sumSize = 0;
        for (Item item : items) {
            sumSize += item.getLength();
        }
        if (remainFreeSpace() < sumSize) {
            throw new RuntimeException("data too long");
        }
        for (Item item : items) {
            if (writeItem(item)) {
                continue;
            } else {
                throw new RuntimeException("Meta Info too Long");
            }
        }
    }

    public void writeInt(int i) {
        bufferWrapper.writeInt(i);
    }

    // 在指定位置写入int
    public void writeIntPos(int i, int position) {
        bufferWrapper.writeIntPos(i, position);
    }

    public void writeByte(byte b) {
        bufferWrapper.writeByte(b);
    }

    public void writeLong(long l) {
        bufferWrapper.writeLong(l);
    }

    public void writeStringWithNull(String s) {
        bufferWrapper.writeStringWithNull(s);
    }

    public void writeBytes(byte[] src, int position) {
        bufferWrapper.writeBytes(src, position);
    }

    public void writeWithNull(byte[] src) {
        bufferWrapper.writeWithNull(src);
    }

    public int readInt() {
        return bufferWrapper.readInt();
    }

    public int readIntPos(int position) {
        return bufferWrapper.readIntPos(position);
    }

    public long readLong() {
        return bufferWrapper.readLong();
    }

    public String readStringWithNull() {
        return bufferWrapper.readStringWithNull();
    }

    public String readStringWithLength(int position) {
        return bufferWrapper.readStringWithLength(position);
    }

    // 读取指定长度的length
    public byte[] readBytes(int position, int length) {
        return bufferWrapper.readBytes(position, length);
    }

    // 读取指定地点的bytes
    public byte[] readBytesWithLength(int position) {
        return bufferWrapper.readBytesWithLength(position);
    }

    public byte[] readBytesWithNull() {
        return bufferWrapper.readBytesWithNull();
    }

    // 剩余多少freeSpace
    public int remainFreeSpace() {
        return pageHeaderData.getUpperOffset() - pageHeaderData.getLowerOffset();
    }

    public int getLength() {
        return length;
    }

    public int getLowerOffset() {
        return pageHeaderData.getLowerOffset();
    }

    public int getUpperOffset() {
        return pageHeaderData.getUpperOffset();
    }

    public int getSpecial() {
        return pageHeaderData.getSpecial();
    }

    public void modifyLowerOffer(int i) {
        pageHeaderData.modifyLowerOffset(i, this);
    }

    public void modifyUpperOffset(int i) {
        pageHeaderData.modifyUpperOffset(i, this);
    }

    public void modifySpecial(int i) {
        pageHeaderData.modifySpecial(i, this);
    }

    public byte[] getBuffer() {
        return bufferWrapper.getBuffer();
    }

    public boolean isDirty() {
        return dirty;
    }

    public Page setDirty(boolean dirty) {
        this.dirty = dirty;
        return this;
    }

    public void clean() {
        bufferWrapper.clean();
        dirty = false;
        pageHeaderData = new PageHeaderData(bufferWrapper.getLength());
        pageHeaderData.write(this);
    }
}
