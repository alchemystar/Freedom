package alchemystar.freedom.transaction.redo;

import alchemystar.freedom.meta.ClusterIndexEntry;
import alchemystar.freedom.meta.IndexDesc;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;
import alchemystar.freedom.meta.TableManager;
import alchemystar.freedom.transaction.OpType;
import alchemystar.freedom.transaction.log.Log;

/**
 * @Author lizhuyang
 */
public class RedoManager {

    public static void redo(Log log) {
        Table table = TableManager.getTable(log.getTableName());
        switch (log.getOpType()) {
            case OpType.insert:
                IndexEntry indexEntry = new ClusterIndexEntry(log.getAfter().getValues());
                indexEntry.setIndexDesc(new IndexDesc(table.getAttributes()));
                table.insert(indexEntry);
                break;
            case OpType.delete:
                table.delete(log.getBefore());
                break;
            case OpType.update:
                // todo
                break;
        }
    }
}
