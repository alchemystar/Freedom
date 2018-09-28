package alchemystar.freedom.access;

import alchemystar.freedom.index.bp.Position;

/**
 * ClusterIndexCursor
 * 索引扫描
 *
 * @Author lizhuyang
 */
public class ClusterIndexCursor extends BaseIndexCursor {

    public ClusterIndexCursor(Position startPos, Position endPos, boolean isEqual) {
        super(startPos, endPos, isEqual);
    }
}
