package alchemystar.freedom.index;

import java.util.List;

import alchemystar.freedom.access.Cursor;
import alchemystar.freedom.index.bp.Position;
import alchemystar.freedom.meta.IndexEntry;

/**
 * Index
 *
 * @Author lizhuyang
 */
public interface Index {

    Cursor searchEqual(IndexEntry key);

    Cursor searchRange(IndexEntry lowKey, IndexEntry upKey);

    Position getFirst(IndexEntry entry, int compareType);   // 查询第一个符合的key

    Position getLast(IndexEntry entry, int compareType); // 查询最后一个符合的key

    List<IndexEntry> getAll(IndexEntry entry); // 查询所有符合条件的key

    int remove(IndexEntry entry);    // 移除所有符合key的数据

    boolean removeOne(IndexEntry entry);   // 删掉一个key

    void insert(IndexEntry entry, boolean isUnique); // 插入

}
