package alchemystar.freedom.test;

import org.junit.Test;

import alchemystar.freedom.sql.SqlExecutor;

/**
 * @Author lizhuyang
 */
public class CreateTest {

    public static  String CREATE_SQL = "create table test (id bigint,name varchar(256),comment varchar(256), PRIMARY "
            + "KEY "
            + "('id'),"
            + "KEY name ('name'));";

    @Test
    public void createTable() {
        SqlExecutor sqlExecutor = new SqlExecutor();
        sqlExecutor.execute(CREATE_SQL, null);
    }
}
