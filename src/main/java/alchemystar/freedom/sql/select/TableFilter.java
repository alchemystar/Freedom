package alchemystar.freedom.sql.select;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;

import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.IndexDesc;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueBoolean;
import alchemystar.freedom.sql.parser.SelectVisitor;
import alchemystar.freedom.sql.parser.WhereVisitor;
import alchemystar.freedom.sql.select.item.SelectExprEval;

/**
 * @Author lizhuyang
 */
public class TableFilter implements ColumnResolver {

    private Table table;

    private TableFilter join;

    // here just for optimizer
    private SQLExpr filterCondition;

    private SQLExpr joinCondition;

    private IndexEntry current;

    private boolean initialized;
    // sql中的别名
    private String alias;

    private Cursor cursor;

    private SelectVisitor selectVisitor;
    // 搜索下界
    private IndexEntry lowEntryKey;
    // 搜索上界
    private IndexEntry upEntryKey;

    private boolean isConflict;

    private int state = 0;

    private int NOT_FOUND = 0;
    private int FOUND = 1;
    private int MOVE_TO_NEXT = 2;

    public boolean next() {
        while (innerNext()) {
            if (joinCondition == null) {
                return true;
            } else if (checkJoinCondition()) {
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    private boolean checkJoinCondition() {
        SelectExprEval eval = new SelectExprEval(joinCondition, selectVisitor);
        Value value = eval.eval();
        if (value instanceof ValueBoolean) {
            return ((ValueBoolean) value).getBoolean();
        }
        throw new RuntimeException("where condition eval not boolean , wrong");
    }

    public boolean innerNext() {
        if (!initialized) {
            buildLowUpEntry();
            // 如果表达式本身冲突,则返回空
            if (isConflict) {
                return false;
            }
            cursor = table.searchRange(lowEntryKey, upEntryKey);
            initialized = true;
        }
        while (true) {
            // 如果没有找到或者需要进行下一个游标遍历,则当前cursor.next
            if (state == NOT_FOUND || state == MOVE_TO_NEXT) {
                current = cursor.next();
                if (current == null) {
                    return false;
                }
                state = FOUND;
                if (join != null) {
                    join.reset();
                }
            }
            if (state == FOUND) {
                if (join != null) {
                    if (join.next()) {
                        return true;
                    } else {
                        state = MOVE_TO_NEXT;
                        continue;
                    }
                } else {
                    state = MOVE_TO_NEXT;
                    return true;
                }
            }
        }
    }

    public void reset() {
        current = null;
        if (cursor != null) {
            cursor.reset();
        }
        if (join != null) {
            join.reset();
        }
    }

    // 根据where visitor 找到最大最小值
    private void buildLowUpEntry() {
        // 如果没有filterCondition ,构造一个全为null的key
        if (filterCondition == null) {
            lowEntryKey = new IndexEntry(new Value[table.getAttributes().length]);
            lowEntryKey.setAllNull(true);
            IndexDesc indexDesc = new IndexDesc(table.getAttributes());
            lowEntryKey.setIndexDesc(indexDesc);
            return;
        }
        WhereVisitor whereVisitor = new WhereVisitor();
        filterCondition.accept(whereVisitor);
        if (whereVisitor.isConflict()) {
            isConflict = true;
            return;
        }
        Value[] lowValues = new Value[table.getAttributes().length];
        // 如果没有updateValues指定,直接是null,让后面没有endPos
        Value[] upValues = null;
        // 如果没有or表达式,则适用low max原则
        if (!whereVisitor.isHasOr()) {
            Map<String, Value> equalMap = getMap(whereVisitor.getEqualMap());
            Map<String, Value> greaterOrEqualMap = getMap(whereVisitor.getGreaterOrEqualMap());
            Map<String, Value> lessOrEqualMap = getMap(whereVisitor.getLessOrEqualMap());
            // 范围赋值
            for (int i = 0; i < table.getAttributes().length; i++) {
                Attribute attribute = table.getAttributes()[i];
                if (greaterOrEqualMap.get(attribute.getName()) != null) {
                    lowValues[i] = greaterOrEqualMap.get(attribute.getName());
                }
                if (lessOrEqualMap.get(attribute.getName()) != null) {
                    if (upValues == null) {
                        upValues = new Value[table.getAttributes().length];
                    }
                    upValues[i] = lessOrEqualMap.get(attribute.getName());
                }
            }
            // equal 赋值
            for (int i = 0; i < table.getAttributes().length; i++) {
                Attribute attribute = table.getAttributes()[i];
                if (equalMap.get(attribute.getName()) != null) {
                    lowValues[i] = equalMap.get(attribute.getName());
                    if (upValues != null) {
                        upValues[i] = equalMap.get(attribute.getName());
                    }
                }
            }
            if (upValues != null) {
                upEntryKey = new IndexEntry(upValues);
            }
        } else {
            // 如果有or表达式,采用顺序扫描,则upEntryKey为null
            upEntryKey = null;
        }

        lowEntryKey = new IndexEntry(lowValues);
        IndexDesc indexDesc = new IndexDesc(table.getAttributes());
        lowEntryKey.setIndexDesc(indexDesc);
        if (upEntryKey != null) {
            upEntryKey.setIndexDesc(indexDesc);
        }

    }

    public Map<String, Value> getMap(Map<SQLExpr, Value> map) {
        Map<String, Value> result = new HashMap<String, Value>();

        for (SQLExpr item : map.keySet()) {
            if (item instanceof SQLPropertyExpr) {
                SQLPropertyExpr propertyExpr = (SQLPropertyExpr) item;
                if (propertyExpr.getOwner().toString().equals(alias)) {
                    result.put(propertyExpr.getName(), map.get(item));
                }
            } else if (item instanceof SQLIdentifierExpr) {
                // 如果是identify 则不需要进行alias判定,因为只有本身一张表
                SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) item;
                result.put(identifierExpr.getName(), map.get(identifierExpr));
            }
        }
        return result;
    }

    public Attribute[] getAttributes() {
        return table.getAttributes();
    }

    public Value getValue(String columnName) {
        return current.getValues()[table.getAttributeIndex(columnName)];
    }

    public TableFilter getTableFilter() {
        return this;
    }

    public String getTableAlias() {
        return alias;
    }

    public IndexEntry getCurrent() {
        return current;
    }

    public Table getTable() {
        return table;
    }

    public TableFilter setTable(Table table) {
        this.table = table;
        return this;
    }

    public TableFilter getJoin() {
        return join;
    }

    public TableFilter setJoin(TableFilter join) {
        this.join = join;
        return this;
    }

    public SQLExpr getFilterCondition() {
        return filterCondition;
    }

    public TableFilter setFilterCondition(SQLExpr filterCondition) {
        this.filterCondition = filterCondition;
        return this;
    }

    public SQLExpr getJoinCondition() {
        return joinCondition;
    }

    public TableFilter setJoinCondition(SQLExpr joinCondition) {
        this.joinCondition = joinCondition;
        return this;
    }

    public void setSelectVisitor(SelectVisitor selectVisitor) {
        this.selectVisitor = selectVisitor;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }


}
