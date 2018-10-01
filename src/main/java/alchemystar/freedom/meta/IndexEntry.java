package alchemystar.freedom.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueBoolean;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueLong;
import alchemystar.freedom.meta.value.ValueString;
import alchemystar.freedom.util.BufferWrapper;

/**
 * 最基本的索引元组
 *
 * @Author lizhuyang
 */
public class IndexEntry {
    // 元组中的值
    protected Value[] values;

    protected IndexDesc indexDesc;

    protected IndexEntry compareEntry;

    private boolean isAllNull;

    public IndexEntry() {
    }

    public IndexEntry(Value[] values) {
        this.values = values;
    }

    public IndexEntry getCompareEntry() {
        if (compareEntry == null) {
            // indexEntry,非聚集索引,最后一个就是rowId
            Value[] tempValue = new Value[values.length - 1];
            for (int i = 0; i < tempValue.length; i++) {
                tempValue[i] = values[i];
            }
            compareEntry = new NotLeafEntry(tempValue);
            compareEntry.setIndexDesc(indexDesc);
        }
        return compareEntry;
    }

    public IndexEntry getDeleteCompareEntry() {
        return this;
    }

    // 获取其byte
    public byte[] getBytes() {
        byte[] bb = new byte[getLength()];
        int position = 0;
        for (Value value : values) {
            System.arraycopy(value.getBytes(), 0, bb, position, value.getLength());
            position += value.getLength();
        }
        return bb;
    }

    public void read(byte[] bytes) {
        BufferWrapper wrapper = new BufferWrapper(bytes);
        List<Value> result = new ArrayList<Value>();
        while (wrapper.remaining() > 0) {
            // 获取类型
            int type = wrapper.readByte();
            byte[] bs;
            Value value;
            switch (type) {
                case Value.STRING:
                    // 获取长度
                    int length = wrapper.readInt();
                    bs = wrapper.readBytes(length);
                    value = new ValueString();
                    break;
                case Value.BOOLEAN:
                    bs = wrapper.readBytes(1);
                    value = new ValueBoolean();
                    break;
                case Value.INT:
                    bs = wrapper.readBytes(4);
                    value = new ValueInt();
                    break;
                case Value.LONG:
                    bs = wrapper.readBytes(8);
                    value = new ValueLong();
                    break;
                default:
                    throw new RuntimeException("Only Support String now");

            }
            value.read(bs);
            result.add(value);
        }
        values = result.toArray(new Value[result.size()]);
    }

    public int compareIndex(IndexEntry indexEntry) {
        IndexEntry compareEntry = indexEntry.getCompareEntry();
        return innerCompare(compareEntry);
    }

    public int compareDeleteIndex(IndexEntry indexEntry) {
        return innerCompare(indexEntry.getDeleteCompareEntry());
    }

    private int innerCompare(IndexEntry indexEntry) {
        int min = values.length < indexEntry.getValues().length ? values.length : indexEntry.getValues().length;
        int comp = 0;
        for (int i = 0; i < min; i++) {
            if (values[i] == null) {
                // 如果本身是null 则是小于
                return -1;
            }
            if (indexEntry.getValues()[i] == null) {
                // 如果比较的key是null 则是大于
                return 1;
            }
            comp = values[i].compare(indexEntry.getValues()[i]);
            if (comp == 0) {
                continue;
            }
        }
        // 到这,表明前面的都相等
        if (comp == 0) {
            if (values.length == indexEntry.getValues().length) {
                return 0;
            }
            // 长度小的在前面
            if (values.length < indexEntry.getValues().length) {
                return -1;
            } else {
                return 1;
            }
        }
        return comp;
    }

    public int getLength() {
        int sum = 0;
        for (Value item : values) {
            sum += item.getLength();
        }
        return sum;
    }

    @Override
    public String toString() {
        return "IndexEntry{" +
                "values=" + Arrays.toString(values) +
                '}';
    }

    public Value[] getValues() {
        return values;
    }

    public IndexEntry setValues(Value[] values) {
        this.values = values;
        return this;
    }

    public boolean isAllNull() {
        return isAllNull;
    }

    public void setAllNull(boolean allNull) {
        isAllNull = allNull;
    }

    public IndexDesc getIndexDesc() {
        return indexDesc;
    }

    public void setIndexDesc(IndexDesc indexDesc) {
        this.indexDesc = indexDesc;
    }
}
