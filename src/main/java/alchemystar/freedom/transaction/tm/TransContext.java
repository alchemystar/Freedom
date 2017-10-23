package alchemystar.freedom.transaction.tm;

/**
 * 事务上下文
 *
 * @Author lizhuyang
 */
public class TransContext {

    // 事务id
    private int txId;
    // 当前事务状态
    private Integer state;

    public TransContext() {
        // 默认不开事务
        state = TransStateConst.NOT_IN_TRANSACTION;
    }
}
