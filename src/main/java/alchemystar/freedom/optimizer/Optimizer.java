package alchemystar.freedom.optimizer;

import alchemystar.freedom.index.Index;
import alchemystar.freedom.meta.IndexDesc;
import alchemystar.freedom.meta.IndexEntry;
import alchemystar.freedom.meta.Table;

/**
 * @Author lizhuyang
 */
public class Optimizer {

    private Table table;

    public Optimizer(Table table) {
        this.table = table;
    }

    public Index chooseIndex(IndexEntry entry) {
        if(entry != null && !entry.isAllNull()) {
            IndexDesc indexDesc = entry.getIndexDesc();
            // 如果包含主键id,则直接用主键id进行查询
            if (indexDesc.getPrimaryAttr() != null && entry.getValues()[indexDesc.getPrimaryAttr().getIndex()] !=
                    null) {
                return table.getClusterIndex();
            }
            // 二级索引选择器优化留待后续优化
            return table.getSecondIndexes().get(0);
        }else {
            return table.getClusterIndex();
        }
    }

}
