package alchemystar.freedom.meta;

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
}
