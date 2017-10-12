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
}
