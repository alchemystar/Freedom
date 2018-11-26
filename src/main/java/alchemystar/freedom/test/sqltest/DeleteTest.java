package alchemystar.freedom.test.sqltest;

import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLStatementParser;
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

    @Test
    public void test2(){
        System.out.println("+==============================+");
        String s = "insert into INTO `android_message_all` (`msg_payload`,`appkey`,`live_time`,`createtime`) VALUES (\"\\\\'\",\"\\'\",\"\\'\",\"\\'\");";
        System.out.println(s);
        SQLStatementParser insertStatement = new SQLStatementParser(s);
        System.out.println(insertStatement);
    }
}
