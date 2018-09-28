package alchemystar.freedom.access;

import alchemystar.freedom.index.Index;
import alchemystar.freedom.index.bp.Position;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueLong;

/**
 * @Author lizhuyang
 */
public class SecondIndexCursor extends BaseIndexCursor {

    private Index clusterIndex;

    public SecondIndexCursor(Position startPos, Position endPos,
                             boolean isEqual) {
        super(startPos, endPos, isEqual);
    }

    @Override
    public IndexEntry next() {
        IndexEntry secondIndexEntry = super.next();
        if (secondIndexEntry == null) {
            return null;
        }
        IndexEntry searchEntry = getSearchEntry(secondIndexEntry);
        // 直接找到其主键即可
        Cursor cursor = clusterIndex.searchEqual(searchEntry);
        if (cursor == null) {
            return null;
        } else {
            // 主键肯定唯一
            return cursor.next();
        }
    }

    private IndexEntry getSearchEntry(IndexEntry indexEntry) {
        // rowId肯定是二级索引的最后一个字段
        ValueLong rowId = (ValueLong) indexEntry.getValues()[indexEntry.getValues().length - 1];
        return new IndexEntry(new Value[] {rowId});
    }


    public Index getClusterIndex() {
        return clusterIndex;
    }

    public void setClusterIndex(Index clusterIndex) {
        this.clusterIndex = clusterIndex;
    }
}
