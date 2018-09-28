package alchemystar.freedom.test.bptest;

import org.junit.Test;

import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.index.bp.BPTree;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.IndexDesc;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueLong;
import alchemystar.freedom.meta.value.ValueString;

/**
 * @Author lizhuyang
 */
public class TableTest {

    @Test
    public void testTable() {
        Table table = genTable();
        IndexDesc tableIndexDesc = new IndexDesc(table.getAttributes());
        int insertSize = 10000;
        for (int i = 1; i <= insertSize; i++) {
            Value[] values = new Value[3];
            values[0] = new ValueLong(i);
            values[1] = new ValueString("alchemystar" + String.valueOf(i % 1000));
            values[2] = new ValueString("comment" + String.valueOf(i));
            // 主键索引,所以走table attribute
            IndexEntry row = new IndexEntry(values);
            row.setIndexDesc(tableIndexDesc);
            table.insert(row);
        }
        Cursor cursor = table.searchEqual(getSecondLowSearchEntry());
        IndexEntry indexEntry;
        int count = 0;
        while ((indexEntry = cursor.next()) != null) {
            count++;
            System.out.println(indexEntry);
        }
        System.out.println(count);
        System.out.println("=============================================>");
        cursor = table.searchEqual(getClusterLowSearchEntry());
        count = 0;
        while ((indexEntry = cursor.next()) != null) {
            count++;
            System.out.println(indexEntry);
        }
        System.out.println(count);
        System.out.println("=============================================>");
        cursor = table.searchRange(getClusterLowSearchEntry(), getClusterUpSearchEntry());
        count = 0;
        while ((indexEntry = cursor.next()) != null) {
            count++;
            System.out.println(indexEntry);
        }
        System.out.println(count);
        System.out.println("=============================================>");
        cursor = table.searchRange(getSecondLowSearchEntry(), getSecondUpSearchEntry());
        count = 0;
        while ((indexEntry = cursor.next()) != null) {
            count++;
            System.out.println(indexEntry);
        }
        System.out.println(count);
    }

    public IndexEntry getSecondLowSearchEntry() {
        IndexEntry indexEntry = new IndexEntry(new Value[] {new ValueString("alchemystar200")});
        Attribute attribute = new Attribute("name", 0, 0, "name");
        indexEntry.setIndexDesc(new IndexDesc(new Attribute[] {attribute}));
        return indexEntry;
    }

    public IndexEntry getSecondUpSearchEntry() {
        IndexEntry indexEntry = new IndexEntry(new Value[] {new ValueString("alchemystar510")});
        Attribute attribute = new Attribute("name", 0, 0, "name");
        indexEntry.setIndexDesc(new IndexDesc(new Attribute[] {attribute}));
        return indexEntry;
    }

    public Table genTable() {
        Table table = new Table();
        table.setAttributes(getTableAttributes());
        BPTree clusterIndex = new BPTree(table, "clusterIndex", getClusterAttributes());
        clusterIndex.setPrimaryKey(true);
        table.setClusterIndex(clusterIndex);
        BPTree secondIndex = new BPTree(table, "secondIndex", getSecondAttributes());
        table.getSecondIndexes().add(secondIndex);
        return table;
    }

    public IndexEntry getClusterLowSearchEntry() {
        IndexEntry indexEntry = new IndexEntry(new Value[] {new ValueLong(100)});
        Attribute attribute = new Attribute("id", 0, 0, "id");
        IndexDesc indexDesc = new IndexDesc(new Attribute[] {attribute});
        indexDesc.setPrimaryAttr(attribute);
        indexEntry.setIndexDesc(indexDesc);
        return indexEntry;
    }

    public IndexEntry getClusterUpSearchEntry() {
        IndexEntry indexEntry = new IndexEntry(new Value[] {new ValueLong(920)});
        Attribute attribute = new Attribute("id", 0, 0, "id");
        IndexDesc indexDesc = new IndexDesc(new Attribute[] {attribute});
        indexDesc.setPrimaryAttr(attribute);
        indexEntry.setIndexDesc(indexDesc);
        return indexEntry;
    }

    public Attribute[] getTableAttributes() {
        Attribute[] attributes = new Attribute[3];
        attributes[0] = new Attribute("id", 1, 0, "id");
        attributes[1] = new Attribute("name", 1, 1, "name");
        attributes[2] = new Attribute("comment", 1, 2, "comment");
        return attributes;
    }

    public Attribute[] getClusterAttributes() {
        Attribute[] attributes = new Attribute[1];
        attributes[0] = new Attribute("id", 1, 0, "id");
        return attributes;
    }

    public Attribute[] getSecondAttributes() {
        Attribute[] attributes = new Attribute[2];
        attributes[0] = new Attribute("name", 1, 0, "name");
        attributes[1] = new Attribute("id", 1, 1, "id");
        return attributes;
    }

}
