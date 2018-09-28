package alchemystar.freedom.sql.select;

import com.alibaba.druid.sql.ast.SQLExpr;

import alchemystar.hero.db.cursor.Cursor;
import alchemystar.hero.meta.Column;
import alchemystar.hero.meta.Row;
import alchemystar.hero.meta.Table;
import alchemystar.hero.meta.value.Value;
import alchemystar.hero.meta.value.ValueBoolean;
import alchemystar.hero.sql.parser.SelectVisitor;
import alchemystar.hero.sql.select.item.SelectExprEval;

/**
 * @Author lizhuyang
 */
public class TableFilter implements ColumnResolver {

    private Table table;

    private TableFilter join;

    // here just for optimizer
    private SQLExpr filterCondition;

    private SQLExpr joinCondition;

    private Row current;

    private boolean initialized;

    private Cursor cursor;

    private SelectVisitor selectVisitor;

    int state = 0;

    int NOT_FOUND = 0;
    int FOUND = 1;
    int MOVE_TO_NEXT = 2;

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
            cursor = CursorFactory.newCursor(table, filterCondition);
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

    public Column[] getColumns() {
        return table.getColumns();
    }

    public Value getValue(String columnName) {
        return current.getValue(table.getColumnIndex(columnName));
    }

    public TableFilter getTableFilter() {
        return this;
    }

    public String getTableAlias() {
        return table.getAlias();
    }

    public Row getCurrent() {
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

    @Override
    public String toString() {
        return "TableFilter{" +
                "table=" + table.getName() + ",alias=" + table.getAlias() + "\n" +
                ", join=" + join +
                '}';
    }
}
