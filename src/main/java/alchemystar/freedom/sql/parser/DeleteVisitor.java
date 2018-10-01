package alchemystar.freedom.sql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.TableManager;
import alchemystar.freedom.sql.select.TableFilter;

/**
 * @Author lizhuyang
 */
public class DeleteVisitor extends SQLASTVisitorAdapter {

    private TableFilter tableFilter;

    private SQLExpr where;

    private WhereVisitor whereVisitor;

    protected Table table;

    private SQLTableSource tableSource;

    public boolean visit(SQLDeleteStatement x) {
        tableSource = x.getTableSource();
        if (!(tableSource instanceof SQLExprTableSource)) {
            throw new RuntimeException("not support this table source type :" + tableSource);
        }
        table = TableManager.getTable(tableSource.toString());
        if (x.getWhere() == null) {
            throw new RuntimeException("delete must have where");
        }
        this.where = x.getWhere();
        whereVisitor = new WhereVisitor();
        x.getWhere().accept(whereVisitor);
        tableFilter = TableManager.newTableFilter((SQLExprTableSource) tableSource, x.getWhere());
        return true;
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    public SQLExpr getWhere() {
        return where;
    }

    public WhereVisitor getWhereVisitor() {
        return whereVisitor;
    }
}
