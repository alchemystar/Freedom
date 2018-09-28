package alchemystar.freedom.access;

import alchemystar.freedom.meta.IndexEntry;

/**
 * 扫描
 *
 * @Author lizhuyang
 */
public interface Cursor {

    IndexEntry next();

    void reset();
}
