package alchemystar.freedom.util;

import java.util.ArrayList;
import java.util.List;

import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueString;

/**
 * ValueConvertUtil
 *
 * @Author lizhuyang
 */
public class ValueConvertUtil {

    public static Value[] convertAttr(Attribute attr) {
        List<Value> list = new ArrayList<Value>();
        list.add(new ValueString(attr.getName()));
        list.add(new ValueInt(attr.getType()));
        list.add(new ValueInt(attr.getIndex()));
        list.add(new ValueString(attr.getComment()));
        return list.toArray(new Value[list.size()]);
    }

    public static Attribute convertValue(Value[] values) {
        Attribute attr = new Attribute();
        attr.setName(((ValueString) values[0]).getString());
        attr.setType(((ValueInt) values[1]).getInt());
        attr.setIndex(((ValueInt) values[2]).getInt());
        attr.setComment(((ValueString) values[3]).getString());
        return attr;
    }
}
