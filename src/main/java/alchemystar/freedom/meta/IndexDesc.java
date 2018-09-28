package alchemystar.freedom.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.store.item.Item;
import alchemystar.freedom.util.ValueConvertUtil;

/**
 * 元组的属性描述
 *
 * @Author lizhuyang
 */
public class IndexDesc {
    // 元组的属性数组
    private Attribute[] attrs;

    private Map<String, Attribute> attrsMap;

    public IndexDesc(Attribute[] attrs) {
        this.attrs = attrs;
        attrsMap = new HashMap<String, Attribute>();
        for (Attribute attr : attrs) {
            attrsMap.put(attr.getName(), attr);
        }
    }

    // 转换为可写入page的item
    public List<Item> getItems() {
        List<Item> list = new LinkedList<Item>();
        for (Attribute attribute : attrs) {
            Value[] values = ValueConvertUtil.convertAttr(attribute);
            IndexEntry indexEntry = new IndexEntry(values);
            Item item = new Item(indexEntry);
            list.add(item);
        }
        return list;
    }

}
