package alchemystar.freedom.sql;

import com.alibaba.druid.sql.ast.SQLStatement;

import alchemystar.freedom.engine.session.Session;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.sql.parser.InsertVisitor;
import alchemystar.transaction.OpType;

/**
 * @Author lizhuyang
 */
public class InsertExecutor {

    private SQLStatement sqlStatement;

    private InsertVisitor insertVisitor;

    public InsertExecutor(SQLStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    public void execute(Session session) {
        init();
        // 必须支持带主键
        IndexEntry indexEntry = insertVisitor.buildInsertEntry();
        insertVisitor.getTable().insert(indexEntry);
        if (session != null) {
            session.addLog(insertVisitor.getTable(), OpType.insert, null, indexEntry);
        }
    }

    public void init() {
        InsertVisitor insertVisitor = new InsertVisitor();
        sqlStatement.accept(insertVisitor);
        this.insertVisitor = insertVisitor;
    }
}
