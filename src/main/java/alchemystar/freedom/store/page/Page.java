package alchemystar.freedom.store.page;

import java.util.List;

import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.store.item.Item;
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

    public boolean spaceEnough(Tuple tuple) {
        Item item = new Item(tuple);
        if (remainFreeSpace() < item.getLength()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean writeTuple(Tuple tuple) {
        return writeItem(new Item(tuple));
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

    public void writeItems(List<Item> items) {
        for (Item item : items) {
            if (this.writeItem(item)) {
                continue;
            } else {
                throw new RuntimeException("Meta Info Two Long");
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
