package alchemystar.freedom.test.sqltest;

import org.junit.Test;

import alchemystar.freedom.sql.SqlExecutor;
import alchemystar.freedom.test.BasicSelectTest;

/**
 * @Author lizhuyang
 */
public class DeleteTest extends BasicSelectTest {

    public static final String deleteSql = "delete from test where id>=1";

    @Test
    public void test() {
        SqlExecutor executor = new SqlExecutor();
        executor.execute(deleteSql, null, null);
    }
}
