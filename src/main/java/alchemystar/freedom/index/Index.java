package alchemystar.freedom.index;

import alchemystar.freedom.meta.Tuple;

/**
 * Index
 *
 * @Author lizhuyang
 */
public interface Index {

    Tuple get(Tuple key);   //查询

    boolean remove(Tuple key);    //移除

    void insert(Tuple key); //插入
}
