package alchemystar.freedom.test.sqltest;

import org.junit.Test;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;

import alchemystar.freedom.sql.SqlExecutor;
import alchemystar.freedom.sql.parser.SelectVisitor;
import alchemystar.freedom.test.BasicSelectTest;

/**
 * @Author lizhuyang
 */
public class SelectTest extends BasicSelectTest {

    public static final String singleSql = "select id,name,comment from test where id=1";

    public static final String wildCardSql = "select * from test ";

    public static final String joinSql =
            "select 'a.id='+a.id,'b.id='+b.id,'c.id='+c.id,'a.name='+a.name from test as a join test as b on a.id=b"
                    + ".id+1 "
                    + "join "
                    + "test as"
                    + " c "
                    + "where a"
                    + ".id>=3 and a.id < 10 and "
                    + " b.id>=2 and b.id < 10 and c.id>=1 and c.id < 10 and a.name < 'alchemystar5'";

    public static void main(String args[]){
        System.out.println(joinSql);
    }


    @Test
    public void selectSingleExecutor() {
        SqlExecutor sqlExecutor = new SqlExecutor();
        sqlExecutor.execute(singleSql, null, null);
    }

    @Test
    public void selectWildCardExecutor() {
        SqlExecutor sqlExecutor = new SqlExecutor();
        sqlExecutor.execute(wildCardSql, null, null);
    }

    @Test
    public void selectJoinExecutor() {
        SqlExecutor sqlExecutor = new SqlExecutor();
        sqlExecutor.execute(joinSql, null, null);
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
