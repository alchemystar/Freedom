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
 * 最基本的元组概念
 *
 * @Author lizhuyang
 */
public class IndexEntry {
    // 元组中的值
    protected Value[] values;

    public IndexEntry() {
    }

    public IndexEntry(Value[] values) {
        this.values = values;
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
            byte[] bs = null;
            Value value = null;
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

    // 和另一个tuple的比较
    // 注意,另一个tuple的可能是个索引,所以两者column的length可能不等
    // 同时,由于最终索引会加两个值表示pageNo和offset,所以,应该比传进来的tuple长度大
    // todo 字典序compare
    public int compareIndex(IndexEntry indexEntry) {
        //        int min = values.length < indexEntry.getValues().length ? values.length : indexEntry.getValues().length;
        //        for (int i = 0; i < min; i++) {
        //            int comp = values[i].compare(indexEntry.getValues()[i]);
        //            if (comp == 0) {
        //                continue;
        //            }
        //            return comp;
        //        }
        //        return 0;
        return compare(indexEntry);
    }

    // 用于索引的tuple比较
    public int compare(IndexEntry indexEntry) {
        int min = values.length < indexEntry.getValues().length ? values.length : indexEntry.getValues().length;
        int comp = 0;
        for (int i = 0; i < min; i++) {
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
}
