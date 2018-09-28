package alchemystar.freedom.meta.value;

import alchemystar.freedom.util.BufferWrapper;

/**
 * @Author lizhuyang
 */
public class ValueInt extends Value {

    private int i;

    public ValueInt() {
    }

    public ValueInt(int i) {
        this.i = i;
    }

    @Override
    public int getLength() {
        // 1 for tupe
        return 1 + 4;
    }

    @Override
    public byte getType() {
        return INT;
    }

    @Override
    public byte[] getBytes() {
        BufferWrapper wrapper = new BufferWrapper(getLength());
        // int type
        wrapper.writeByte(INT);
        // for the value string
        wrapper.writeInt(i);
        return wrapper.getBuffer();
    }

    @Override
    public void read(byte[] bytes) {
        BufferWrapper wrapper = new BufferWrapper(bytes);
        i = wrapper.readInt();
    }

    @Override
    public String toString() {
        return String.valueOf(i);
    }

    public int getInt() {
        return i;
    }

    public ValueInt setInt(int i) {
        this.i = i;
        return this;
    }

    @Override
    public int compare(Value value) {
        int toCompare;
        if (value instanceof ValueLong) {
            toCompare = ((ValueLong) value).getInt();
        } else {
            toCompare = (((ValueInt) value).getInt());
        }
        if (i > toCompare) {
            return 1;
        }
        if (i == toCompare) {
            return 0;
        }
        return -1;
    }

    @Override
    public String getString() {
        return String.valueOf(i);
    }

    @Override
    public Value add(Value v) {
        return new ValueInt(i + v.getInt());
    }

    public Value subtract(Value v) {
        return new ValueInt(i - v.getInt());
    }

    public Value divide(Value v) {
        return new ValueInt(i / v.getInt());
    }

    public Value multiply(Value v) {
        return new ValueInt(i * v.getInt());
    }

    public Value concat(Value v) {
        return new ValueString(this.toString() + v.toString());
    }

    public long getLong() {
        return (long) i;
    }

}
