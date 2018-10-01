package alchemystar.freedom.sql;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLStatement;

import alchemystar.freedom.engine.net.handler.frontend.FrontendConnection;
import alchemystar.freedom.engine.net.response.OkResponse;
import alchemystar.freedom.engine.session.Session;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueBoolean;
import alchemystar.freedom.sql.parser.DeleteVisitor;
import alchemystar.freedom.sql.select.TableFilter;
import alchemystar.freedom.sql.select.item.SelectExprEval;
import alchemystar.transaction.OpType;

/**
 * @Author lizhuyang
 */
public class DeleteExecutor {

    private SQLStatement sqlStatement;

    private DeleteVisitor deleteVisitor;

    private Table table;

    private FrontendConnection con;

    public DeleteExecutor(SQLStatement sqlStatement, FrontendConnection con) {
        this.sqlStatement = sqlStatement;
        this.con = con;
    }

    public void execute(Session session) {
        init();
        TableFilter tableFilter = deleteVisitor.getTableFilter();
        List<IndexEntry> toDelete = new ArrayList<IndexEntry>();
        // 必须先拿出来再删除,不然会引起删除的position变化
        while (tableFilter.next()) {
            if (checkWhereCondition()) {
                toDelete.add(tableFilter.getCurrent());
            }
        }
        for (IndexEntry delItem : toDelete) {
            table.delete(delItem);
            if (session != null) {
                session.addLog(table, OpType.delete, delItem, null);
            }
        }
        OkResponse.responseWithAffectedRows(con, toDelete.size());
    }

    private boolean checkWhereCondition() {
        SelectExprEval eval = new SelectExprEval(deleteVisitor.getWhere(), null);
        eval.setSimpleTableFilter(deleteVisitor.getTableFilter());
        Value value = eval.eval();
        if (value instanceof ValueBoolean) {
            return ((ValueBoolean) value).getBoolean();
        }
        throw new RuntimeException("where condition eval not boolean , wrong");
    }

    public void init() {
        DeleteVisitor deleteVisitor = new DeleteVisitor();
        sqlStatement.accept(deleteVisitor);
        this.deleteVisitor = deleteVisitor;
        this.table = deleteVisitor.getTableFilter().getTable();
    }
}
