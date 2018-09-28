package alchemystar.freedom.test;

import org.junit.Test;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import alchemystar.hero.sql.SelectExecutor;
import alchemystar.hero.sql.parser.SelectVisitor;

/**
 * @Author lizhuyang
 */
public class SelectTest {

    public static final String singleSql = "select id,name,id,name,id,name from test where id='1'";

    public static final String joinSql =
            "select 'a.id='+a.id,'b.id='+b.id,'c.id='+c.id from test as a join test as b on a.id=b.id+1 join test as c "
                    + "on b.id = c.id+1 "
                    + "where a"
                    + ".id>=3 and"
                    + " b.id>=2";

    @Test
    public void selectSingleExecutor() {
        SelectExecutor selectExecutor = new SelectExecutor(singleSql);
        selectExecutor.query();
    }

    @Test
    public void selectJoinExecutor() {
        SelectExecutor selectExecutor = new SelectExecutor(joinSql);
        selectExecutor.query();
    }

    @Test
    public void selectSingle() {
        selectParse(singleSql);

    }

    @Test
    public void selectJoin() {
        selectParse(joinSql);
    }

    public void selectParse(String selectSQL) {
        SQLStatementParser parser = new SQLStatementParser(selectSQL);
        SQLStatement sqlStatement = parser.parseStatement();
        SelectVisitor selectVisitor = new SelectVisitor();
        sqlStatement.accept(selectVisitor);
        System.out.println(selectVisitor.getTableFilter());

    }
}
