package alchemystar.freedom.test;

import org.junit.Before;
import org.junit.Test;

import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.TableManager;
import alchemystar.freedom.sql.SqlExecutor;
import alchemystar.freedom.test.bptest.BasicGenTable;

/**
 * @Author lizhuyang
 */
public class InsertTest extends BasicGenTable {

    public static final String insertSqlTemplate = "insert into test (id,name,comment) values (?,?,?)";

    @Before
    public void init() {
        Table table = genTable();
        TableManager.addTable(table);
    }

    @Test
    public void test() {
        for (int i = 0; i < 1000; i++) {
            String insertSql = insertSqlTemplate.replace("?", String.valueOf(i)).replace("?", "alchemystar" + String
                    .valueOf(i)
                    + "comment" + String.valueOf(i));
            SqlExecutor sqlExecutor = new SqlExecutor();
            sqlExecutor.execute(insertSql, null);
        }

        System.out.println("insert okay");
    }
}
