package alchemystar.freedom.meta.value;

import alchemystar.freedom.util.BufferWrapper;

/**
 * ValueString
 *
 * @Author lizhuyang
 */
public class ValueString extends Value {

    private String s;

    public ValueString() {
    }

    public ValueString(String s) {
        this.s = s;
    }

    // [type][length][data]
    @Override
    public int getLength() {
        return 1 + 4 + s.length();
    }

    @Override
    public byte getType() {
        return STRING;
    }

    @Override
    public byte[] getBytes() {
        BufferWrapper wrapper = new BufferWrapper(getLength());
        wrapper.writeByte(getType());
        // 此处写入的是string的长度
        wrapper.writeStringLength(s);
        return wrapper.getBuffer();
    }

    public void read(byte[] bytes) {
        s = new String(bytes);
    }

    @Override
    public String toString() {
        return s;
    }

    public String getString() {
        return s;
    }

    public ValueString setString(String s) {
        this.s = s;
        return this;
    }

    @Override
    public int compare(Value value) {
        return s.compareTo(((ValueString) value).getString());
    }

    @Override
    public Value add(Value v) {
        if (v instanceof ValueString) {
            return new ValueString(s + ((ValueString) v).getString());
        } else if (v instanceof ValueInt) {
            return new ValueString(s + String.valueOf(((ValueInt) v).getInt()));
        } else if (v instanceof ValueLong) {
            return new ValueString(s + String.valueOf(((ValueLong) v).getLong()));
        } else if (v instanceof ValueBoolean) {
            return new ValueString(s + String.valueOf(((ValueBoolean) v).getBoolean()));
        }
        throw new RuntimeException("not support this type , valueType=" + v.getType());
    }

    @Override
    public Value concat(Value v) {
        if (v instanceof ValueString) {
            return new ValueString(s + ((ValueString) v).getString());
        } else if (v instanceof ValueInt) {
            return new ValueString(s + String.valueOf(((ValueInt) v).getInt()));
        } else if (v instanceof ValueLong) {
            return new ValueString(s + String.valueOf(((ValueLong) v).getLong()));
        } else if (v instanceof ValueBoolean) {
            return new ValueString(s + String.valueOf(((ValueBoolean) v).getBoolean()));
        }
        throw new RuntimeException("not support this type , valueType=" + v.getType());
    }

    public static void main(String args[]) {
        System.out.println("alchemystar1".compareTo("alchemystar10"));
    }
}
