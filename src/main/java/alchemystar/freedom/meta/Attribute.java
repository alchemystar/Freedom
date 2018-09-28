package alchemystar.freedom.meta;

import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueBoolean;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueLong;
import alchemystar.freedom.meta.value.ValueString;

/**
 * 属性
 *
 * @Author lizhuyang
 */
public class Attribute {
    // 属性名称
    private String name;
    // 属性类型
    private int type;
    // 在TupleDesc中的位置
    private int index;
    // 注释
    private String comment;

    private boolean isPrimaryKey;

    public Attribute() {
    }

    public Attribute(String name, int type, int index, String comment) {
        this.name = name;
        this.type = type;
        this.index = index;
        this.comment = comment;
    }

    public Value getDefaultValue() {

        switch (type) {
            case Value.STRING:
                return new ValueString("");
            case Value.INT:
                return new ValueInt(0);
            case Value.LONG:
                return new ValueLong(0);
            case Value.BOOLEAN:
                return new ValueBoolean(false);
            default:
                throw new RuntimeException("not support this type :" + type);
        }
    }

    public String getName() {
        return name;
    }

    public Attribute setName(String name) {
        this.name = name;
        return this;
    }

    public int getType() {
        return type;
    }

    public Attribute setType(int type) {
        this.type = type;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public Attribute setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Attribute setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }
}
