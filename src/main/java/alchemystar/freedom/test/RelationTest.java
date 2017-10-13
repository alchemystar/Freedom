package alchemystar.freedom.test;

import org.junit.Test;

import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.access.SeqCursor;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.Relation;
import alchemystar.freedom.meta.Tuple;
import alchemystar.freedom.meta.TupleDesc;
import alchemystar.freedom.meta.factory.RelFactory;
import alchemystar.freedom.meta.value.Value;

/**
 * @Author lizhuyang
 */
public class RelationTest {

    @Test
    public void relationTest() {
        // 写入relation
        //        Relation relation = RelFactory.getInstance().newRelation("t_freedom");
        //        relation.setTupleDesc(getTupleDesc());
        //        relation.open();
        //        relation.writeMeta();
        //        relation.writePageOffInfo();
        //        relation.close();
        // 读取relation
        Relation relation2 = RelFactory.getInstance().newRelation("t_freedom");
        relation2.open();
        relation2.readMeta();
        relation2.readPageOffInfo();
        relation2.loadFromDisk();
        //        Value[] values = new Value[2];
        //        values[0] = new ValueLong(1);
        //        values[1] = new ValueString("alchemystar");
        //        Item item = new Item(new Tuple(values));
        //        for (int i = 0; i < 10000; i++) {
        //            relation2.insert(item);
        //        }
        //        relation2.flushToDisk();
        relation2.loadFromDisk();
        Cursor scanner = new SeqCursor(relation2);
        while (true) {
            Tuple tuple = scanner.getNext();
            if (tuple == null) {
                break;
            } else {
                System.out.println(tuple);
            }
        }
    }

    public TupleDesc getTupleDesc() {
        Attribute[] attributes = new Attribute[2];
        Attribute attrId = new Attribute();
        attrId.setName("id");
        attrId.setType(Value.LONG);
        attrId.setIndex(0);
        attrId.setComment("primary key");

        Attribute attrName = new Attribute();
        attrName.setName("name");
        attrName.setType(Value.STRING);
        attrName.setIndex(1);
        attrName.setComment("name key");

        attributes[0] = attrId;
        attributes[1] = attrName;

        return new TupleDesc(attributes);
    }
}
