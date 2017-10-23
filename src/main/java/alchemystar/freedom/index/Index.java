package alchemystar.freedom.index;

import java.util.List;

import alchemystar.freedom.index.bp.GetRes;
import alchemystar.freedom.meta.Tuple;

/**
 * Index
 *
 * @Author lizhuyang
 */
public interface Index {

    List<Tuple> getAll(Tuple key); // 查询所有符合条件的key

    GetRes getFirst(Tuple key);   // 查询第一个符合的key

    int remove(Tuple key);    // 移除所有符合key的数据

    boolean removeOne(Tuple key);   // 删掉一个key

    void insert(Tuple key, boolean isUnique); // 插入

}
