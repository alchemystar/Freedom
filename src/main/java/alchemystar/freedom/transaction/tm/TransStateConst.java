package alchemystar.freedom.transaction.tm;

/**
 * 事务常量
 *
 * @Author lizhuyang
 */
public class TransStateConst {

    public static final Integer NOT_IN_TRANSACTION = 0;

    public static final Integer IN_TRANSACTION = 1;

    public static final Integer ROLLBACK = 2;

    public static final Integer COMMITED = 3;
}
