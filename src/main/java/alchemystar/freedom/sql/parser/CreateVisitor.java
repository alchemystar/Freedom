package alchemystar.freedom.sql.parser;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import alchemystar.freedom.index.BaseIndex;
import alchemystar.freedom.index.bp.BPTree;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.value.Value;

/**
 * @Author lizhuyang
 */
public class CreateVisitor extends SQLASTVisitorAdapter {

    private List<MySqlKey> mySqlKeys = new ArrayList<MySqlKey>();
    private Table table;

    @Override
    public boolean visit(SQLCreateTableStatement x) {
        SQLTableSource sqlTableSource = x.getTableSource();
        if (!(sqlTableSource instanceof SQLExprTableSource)) {
            throw new RuntimeException("not support this table source " + sqlTableSource);
        }
        table = new Table();
        table.setName((sqlTableSource).toString());
        table.setAttributes(getAttributes(x.getTableElementList()));
        BPTree clusterIndex = new BPTree(table, "clusterIndex", new Attribute[] {table.getPrimaryAttribute()});
        clusterIndex.setPrimaryKey(true);
        table.setClusterIndex(clusterIndex);
        table.setSecondIndexes(buildSecondIndexes());
        return false;
    }

    private List<BaseIndex> buildSecondIndexes() {
        List<BaseIndex> result = new ArrayList<BaseIndex>();
        for (MySqlKey mySqlKey : mySqlKeys) {
            result.add(buildOneSecondIndex(mySqlKey));
        }
        return result;
    }

    private BaseIndex buildOneSecondIndex(MySqlKey mySqlKey) {
        List<SQLSelectOrderByItem> items = mySqlKey.getColumns();
        // +1 for rowId
        Attribute[] attributes = new Attribute[items.size() + 1];
        for (int i = 0; i < items.size(); i++) {
            String indexAttrName = items.get(i).getExpr().toString().replace("'", "");
            Attribute temp = table.getAttributes()[table.getAttributeIndex(indexAttrName)];
            attributes[i] = temp;
        }
        Attribute oldPrimaryAttribute = table.getPrimaryAttribute();
        int lastIndex = items.size();
        Attribute rowIdAttribute = new Attribute(oldPrimaryAttribute.getName(), oldPrimaryAttribute.getType(),
                lastIndex, oldPrimaryAttribute.getComment());
        attributes[items.size()] = rowIdAttribute;
        return new BPTree(table, mySqlKey.getName().toString(), attributes);
    }

    private Attribute[] getAttributes(List<SQLTableElement> list) {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        String primaryKeyName = "";
        for (int i = 0; i < list.size(); i++) {
            SQLTableElement temp = list.get(i);
            if (temp instanceof SQLColumnDefinition) {
                SQLColumnDefinition element = (SQLColumnDefinition) temp;
                SQLExpr commentExpr = element.getComment();
                String comment = commentExpr == null ? "" : commentExpr.toString();
                Attribute attribute = new Attribute(element.getName().toString(), getType(element), i, comment);
                attributeList.add(attribute);
            } else if (temp instanceof MySqlPrimaryKey) {
                MySqlPrimaryKey element = (MySqlPrimaryKey) temp;
                primaryKeyName = element.getColumns().get(0).getExpr().toString().replaceAll("'", "");
            } else if (temp instanceof MySqlKey) {
                mySqlKeys.add((MySqlKey) temp);
            }
        }
        // 设置primaryKey
        for (Attribute item : attributeList) {
            if (item.getName().equalsIgnoreCase(primaryKeyName)) {
                item.setPrimaryKey(true);
            }
        }
        return attributeList.toArray(new Attribute[] {});
    }

    public int getType(SQLColumnDefinition element) {
        String dataType = element.getDataType().getName();
        if (dataType.equalsIgnoreCase("int")) {
            return Value.INT;
        } else if (dataType.equalsIgnoreCase("bigint") || dataType.equalsIgnoreCase("long")) {
            return Value.LONG;
        } else if (dataType.equalsIgnoreCase("varchar") || dataType.equalsIgnoreCase("String")) {
            return Value.STRING;
        } else if (dataType.equalsIgnoreCase("bool") || dataType.equalsIgnoreCase("boolean")) {
            return Value.BOOLEAN;
        } else {
            throw new RuntimeException("not support this type :" + dataType);
        }
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
