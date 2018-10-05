package alchemystar.freedom.transaction;

/**
 * TrxState
 *
 * @Author lizhuyang
 */
public interface TrxState {
    // 事务未开始
    int TRX_STATE_NOT_STARTED = 0;
    // 事务进行中
    int TRX_STATE_ACTIVE = 1;
    // 暂时不用 for 2PC/XA
    int TRX_STATE_PREPARED = 2;
    // 事务已提交
    int TRX_COMMITTED = 3;

}
