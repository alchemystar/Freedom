package alchemystar.freedom.meta;

import alchemystar.freedom.meta.value.Value;

/**
 * @Author lizhuyang
 */
public class NotLeafEntry extends IndexEntry {

    public NotLeafEntry(Value[] values) {
        super(values);
    }

    // cluster的非叶子节点,其本身就是compare key
    public IndexEntry getCompareEntry() {
        return this;
    }


}
