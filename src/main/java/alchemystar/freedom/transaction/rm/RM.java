package alchemystar.freedom.transaction.rm;

/**
 * RM
 *
 * @Author lizhuyang
 */
public interface RM {

    // 返回lsn
    int prepare();

    void undo(int lsn);

    void redo(int lsn);

    // TM重启的时候,传递给RM以LSN
    void tmStartUp(int lsn);

}
