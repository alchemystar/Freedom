package alchemystar.freedom.sql;

import com.alibaba.druid.sql.ast.SQLStatement;

import alchemystar.freedom.meta.TableManager;
import alchemystar.freedom.sql.parser.CreateVisitor;

/**
 * @Author lizhuyang
 */
public class CreateExecutor {

    private SQLStatement sqlStatement;

    private CreateVisitor createVisitor;

    public CreateExecutor(SQLStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    public void execute() {
        init();
        TableManager.addTable(createVisitor.getTable(), true);
    }

    public void init() {
        CreateVisitor createVisitor = new CreateVisitor();
        sqlStatement.accept(createVisitor);
        this.createVisitor = createVisitor;
    }
}
