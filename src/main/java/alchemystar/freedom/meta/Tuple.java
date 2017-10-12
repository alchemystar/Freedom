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
public class Tuple {
    // 元组中的值
    protected Value[] values;

    public Tuple() {
    }

    public Tuple(Value[] values) {
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
    // todo 字典序compare
    public int compare(Tuple tuple) {
        int min = values.length < tuple.getValues().length ? values.length : tuple.getValues().length;
        for (int i = 0; i < min; i++) {
            int comp = values[i].compare(tuple.getValues()[i]);
            if (comp == 0) {
                continue;
            }
            return comp;
        }
        return 0;
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
        return "Tuple{" +
                "values=" + Arrays.toString(values) +
                '}';
    }

    public Value[] getValues() {
        return values;
    }

    public Tuple setValues(Value[] values) {
        this.values = values;
        return this;
    }
}
