package alchemystar.transaction.undo;

import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.TableManager;
import alchemystar.transaction.OpType;
import alchemystar.transaction.log.Log;

/**
 * @Author lizhuyang
 */
public class UndoManager {

    public static void undo(Log log) {
        Table table = TableManager.getTable(log.getTableName());
        switch (log.getOpType()) {
            case OpType.insert:
                undoInsert(table, log);
                break;
            case OpType.update:
                undoUpdate(table, log);
                break;
            case OpType.delete:
                undoDelete(table, log);
                break;
        }
    }

    public static void undoInsert(Table table, Log log) {
        // insert undo = > delete
        table.delete(log.getAfter());
    }

    public static void undoUpdate(Table table, Log log) {
        // todo
    }

    public static void undoDelete(Table table, Log log) {
        table.insert(log.getBefore());
    }
}
