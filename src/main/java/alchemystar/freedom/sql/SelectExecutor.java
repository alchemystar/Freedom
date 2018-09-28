package alchemystar.freedom.sql;

import java.util.List;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import alchemystar.hero.meta.value.Value;
import alchemystar.hero.meta.value.ValueBoolean;
import alchemystar.hero.sql.parser.SelectVisitor;
import alchemystar.hero.sql.select.TableFilter;
import alchemystar.hero.sql.select.item.SelectExprEval;

/**
 * @Author lizhuyang
 */
public class SelectExecutor {

    private String sql;
    private SelectVisitor selectVisitor;

    public SelectExecutor(String sql) {
        this.sql = sql;
    }

    public void query() {
        init();
        TableFilter tableFilter = selectVisitor.getTableFilter();
        List<SQLSelectItem> items = selectVisitor.getSelectItems();
        int count = 0;
        while (tableFilter.next()) {
            if (checkWhereCondition()) {
                count++;
                for (SQLSelectItem item : items) {
                    SelectExprEval itemEval = new SelectExprEval(item.getExpr(), selectVisitor);
                    System.out.print(itemEval.eval());
                    System.out.print(",");
                }
                System.out.println();
            }
        }
        System.out.println("AllCount = " + count);
    }
    private boolean checkWhereCondition() {
        SelectExprEval eval = new SelectExprEval(selectVisitor.getWhereCondition(), selectVisitor);
        Value value = eval.eval();
        if (value instanceof ValueBoolean) {
            return ((ValueBoolean) value).getBoolean();
        }
        throw new RuntimeException("where condition eval not boolean , wrong");
    }


    public void init() {
        SQLStatementParser parser = new SQLStatementParser(sql);
        SQLStatement sqlStatement = parser.parseStatement();
        SelectVisitor selectVisitor = new SelectVisitor();
        sqlStatement.accept(selectVisitor);
        this.selectVisitor = selectVisitor;
    }
}
