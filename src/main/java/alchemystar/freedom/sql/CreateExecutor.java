package alchemystar.freedom.sql;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import alchemystar.freedom.sql.parser.CreateVisitor;

/**
 * @Author lizhuyang
 */
public class CreateExecutor {

    private String sql;

    private CreateVisitor createVisitor;

    public CreateExecutor(String sql) {
        this.sql = sql;
    }

    public void execute() {
        init();
    }

    public void init() {
        SQLStatementParser parser = new SQLStatementParser(sql);
        SQLStatement sqlStatement = parser.parseStatement();
        CreateVisitor createVisitor = new CreateVisitor();
        sqlStatement.accept(createVisitor);
        this.createVisitor = createVisitor;
    }
}
