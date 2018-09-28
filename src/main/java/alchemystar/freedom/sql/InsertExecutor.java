package alchemystar.freedom.sql;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.sql.parser.InsertVisitor;

/**
 * @Author lizhuyang
 */
public class InsertExecutor {

    private SQLStatement sqlStatement;

    private InsertVisitor insertVisitor;

    public InsertExecutor(SQLStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    public void execute() {
        init();
        // 必须支持带主键
        IndexEntry indexEntry = insertVisitor.buildInsertEntry();
        insertVisitor.getTable().insert(indexEntry);
    }

    public void init() {
        InsertVisitor insertVisitor = new InsertVisitor();
        sqlStatement.accept(insertVisitor);
        this.insertVisitor = insertVisitor;
    }
}
