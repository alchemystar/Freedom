package alchemystar.freedom.store.page;

import alchemystar.freedom.config.SystemConfig;

/**
 * PageHeaderData
 *
 * @Author lizhuyang
 */
public class PageHeaderData {
    // todo 变的时候 需要同步更新这里
    public static final Integer PAGE_HEADER_SIZE = 24;
    // Page开头的magicWorld
    private String magicWord = "Session";
    // free space的起始偏移
    private int lowerOffset;
    // 指向pageHeader中的lowerOffset起始位置
    public static final int LOWER_POINTER = 8;
    // free space的最终偏移
    private int upperOffset;
    // 指向pageHeader中的upperOffset起始位置
    public static final int UPPER_POINTER = 12;
    // special space的起始偏移
    private int special;
    // 指向pageHeader中的special起始位置
    public static final int SPECIAL_POINTER = 16;
    // 记录元组的数量
    private int tupleCount;
    public static final int TUPLE_COUNT_POINTER = 20;

    // 记录header长度
    private int headerLength;

    public PageHeaderData(int size) {
        int magicWorldLength = magicWord.getBytes().length + 1;
        lowerOffset = magicWorldLength + 4 + 4 + 4 + 4;
        // 给special 64字节的空间
        upperOffset = size - SystemConfig.DEFAULT_SPECIAL_POINT_LENGTH;
        special = upperOffset;
        headerLength = lowerOffset;
    }

    public void modifyLowerOffset(int i, Page page) {
        lowerOffset = i;
        // 修改byte数组位置
        page.writeIntPos(i, LOWER_POINTER);
    }

    public void modifyUpperOffset(int i, Page page) {
        upperOffset = i;
        // 修改byte数组位置
        page.writeIntPos(i, UPPER_POINTER);
    }

    public void modifySpecial(int i, Page page) {
        upperOffset = i;
        // 修改byte数组位置
        page.writeIntPos(i, SPECIAL_POINTER);
    }

    public void addTupleCount(Page page) {
        int count = page.readIntPos(TUPLE_COUNT_POINTER);
        count++;
        // 修改tupe的数量
        page.writeIntPos(count, TUPLE_COUNT_POINTER);
        tupleCount = count;
    }

    public void decTupleCount(Page page) {
        int count = page.readIntPos(TUPLE_COUNT_POINTER);
        count--;
        // 修改tupe的数量
        page.writeIntPos(count, TUPLE_COUNT_POINTER);
        tupleCount = count;
    }

    void write(Page page) {
        page.writeStringWithNull(magicWord);
        page.writeInt(lowerOffset);
        page.writeInt(upperOffset);
        page.writeInt(special);
        page.writeInt(tupleCount);
    }

    public static PageHeaderData read(Page page) {
        PageHeaderData pageHeaderData = new PageHeaderData(page.getLength());
        pageHeaderData.magicWord = page.readStringWithNull();
        pageHeaderData.lowerOffset = page.readInt();
        pageHeaderData.upperOffset = page.readInt();
        pageHeaderData.special = page.readInt();
        pageHeaderData.tupleCount = page.readInt();
        return pageHeaderData;
    }

    public int getUpperOffset() {
        return upperOffset;
    }

    public PageHeaderData setUpperOffset(int upperOffset) {
        this.upperOffset = upperOffset;
        return this;
    }

    public int getLowerOffset() {
        return lowerOffset;
    }

    public PageHeaderData setLowerOffset(int lowerOffset) {
        this.lowerOffset = lowerOffset;
        return this;
    }

    public int getSpecial() {
        return special;
    }

    public PageHeaderData setSpecial(int special) {
        this.special = special;
        return this;
    }

    public int getTupleCount() {
        return tupleCount;
    }

    public int getLength() {
        return headerLength;
    }
}
