package alchemystar.freedom.sql;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;

/**
 * @Author lizhuyang
 */
public class SqlExecutor {

    public void execute(String sql) {
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement sqlStatement = parser.parseStatement();
        if (sqlStatement instanceof SQLCreateTableStatement) {
            CreateExecutor createExecutor = new CreateExecutor(sqlStatement);
            createExecutor.execute();
            return;
        } else if (sqlStatement instanceof SQLInsertStatement) {
            InsertExecutor insertExecutor = new InsertExecutor(sqlStatement);
            insertExecutor.execute();
            return;
        } else if (sqlStatement instanceof SQLSelectStatement) {
            SelectExecutor selectExecutor = new SelectExecutor(sqlStatement);
            selectExecutor.execute();
            return;
        }
    }
}
