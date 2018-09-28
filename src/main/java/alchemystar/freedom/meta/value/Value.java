package alchemystar.freedom.meta.value;

/**
 * Value
 *
 * @Author lizhuyang
 */
public abstract class Value {

    public static final byte UNKNOWN = 100;
    public static final byte STRING = 1;
    public static final byte INT = 2;
    public static final byte LONG = 3;
    public static final byte BOOLEAN = 4;

    public abstract int getLength();

    public abstract byte getType();

    public abstract byte[] getBytes();

    public abstract void read(byte[] bytes);

    public abstract int compare(Value value);

    public ValueBoolean and(Value value) {
        if (!(this instanceof ValueBoolean)) {
            throw new RuntimeException("left value must be boolean");
        }
        if (!(value instanceof ValueBoolean)) {
            throw new RuntimeException("right value must be boolean");
        }

        boolean result = ((ValueBoolean) this).getBoolean() && ((ValueBoolean) value).getBoolean();
        return new ValueBoolean(result);
    }

    public ValueBoolean or(Value value) {
        if (!(this instanceof ValueBoolean)) {
            throw new RuntimeException("left value must be boolean");
        }
        if (!(value instanceof ValueBoolean)) {
            throw new RuntimeException("right value must be boolean");
        }

        boolean result = ((ValueBoolean) this).getBoolean() || ((ValueBoolean) value).getBoolean();
        return new ValueBoolean(result);
    }

    public ValueBoolean equality(Value value) {
        if (compare(value) == 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    public ValueBoolean greaterThanOrEqual(Value value) {
        if (compare(value) >= 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    public ValueBoolean greaterThan(Value value) {
        if (compare(value) > 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    public ValueBoolean lessThanOrEqual(Value value) {
        if (compare(value) <= 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    public ValueBoolean lessThan(Value value) {
        if (compare(value) < 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    public Value add(Value v) {
        throw new RuntimeException("UnSupport Plus Function");
    }

    public int getInt() {
        throw new RuntimeException("UnSupport get int");
    }

    public long getLong() {
        throw new RuntimeException("UnSupport get long");
    }

    public abstract String getString();

    public Value subtract(Value v) {
        throw new RuntimeException("UnSupport Minus Function");
    }

    public Value divide(Value v) {
        throw new RuntimeException("UnSupport divide Function");
    }

    public Value multiply(Value v) {
        throw new RuntimeException("UnSupport multiply Function");
    }

    public Value concat(Value v) {
        throw new RuntimeException("UnSupport concat Function");
    }
}
