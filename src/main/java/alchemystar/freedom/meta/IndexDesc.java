package alchemystar.freedom.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * 元组的属性描述
 *
 * @Author lizhuyang
 */
public class IndexDesc {
    // 元组的属性数组
    private Attribute[] attrs;
    // 主键属性
    private Attribute primaryAttr;

    private Map<String, Attribute> attrsMap = new HashMap<String, Attribute>();

    public IndexDesc(Attribute[] attrs) {
        this.attrs = attrs;
        attrsMap = new HashMap<String, Attribute>();
        for (Attribute attr : attrs) {
            attrsMap.put(attr.getName(), attr);
            if (attr.isPrimaryKey()) {
                primaryAttr = attr;
            }
        }
    }

    public Attribute getPrimaryAttr() {
        return primaryAttr;
    }

    public void setPrimaryAttr(Attribute primaryAttr) {
        this.primaryAttr = primaryAttr;
    }

    public Attribute[] getAttrs() {
        return attrs;
    }

    public void setAttrs(Attribute[] attrs) {
        this.attrs = attrs;
    }

    public Map<String, Attribute> getAttrsMap() {
        return attrsMap;
    }

    public void setAttrsMap(Map<String, Attribute> attrsMap) {
        this.attrsMap = attrsMap;
    }
}
