package alchemystar.freedom.sql.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.TableManager;
import alchemystar.freedom.sql.select.TableFilter;

/**
 * @Author lizhuyang
 */
public class SelectVisitor extends SQLASTVisitorAdapter {

    private TableFilter tableFilter;
    private List<SQLSelectItem> selectItems;
    private SQLExpr whereCondition;
    private Map<String, TableFilter> aliasMap = new HashMap<String, TableFilter>();

    public boolean visit(SQLSelectQueryBlock x) {
        whereCondition = x.getWhere();
        initTableFilter(x.getFrom());
        // select必须在下面,已处理alias信息
        handleWildCard(x.getSelectList());
        return false;
    }

    private void handleWildCard(List<SQLSelectItem> sqlSelectItems) {
        selectItems = new ArrayList<SQLSelectItem>();
        for (SQLSelectItem sqlSelectItem : sqlSelectItems) {
            if (sqlSelectItem.getExpr() instanceof SQLAllColumnExpr) {
                handleOneWildCard(sqlSelectItem);
            } else {
                if (sqlSelectItem.getExpr() instanceof SQLPropertyExpr) {
                    SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) sqlSelectItem.getExpr();
                    if (sqlPropertyExpr.getSimpleName().equals("*")) {
                        handleOneWildCard(sqlSelectItem);
                    } else {
                        selectItems.add(sqlSelectItem);
                    }
                } else {
                    selectItems.add(sqlSelectItem);
                }
            }
        }
    }

    private void handleOneWildCard(SQLSelectItem sqlSelectItem) {
        SQLExpr sqlExpr = sqlSelectItem.getExpr();
        Attribute[] attributes = null;
        String owner = null;
        if (sqlExpr instanceof SQLPropertyExpr) {
            owner = ((SQLPropertyExpr) sqlExpr).getOwner().toString();
            attributes = aliasMap.get(owner).getAttributes();
        } else if (sqlExpr instanceof SQLAllColumnExpr) {
            attributes = tableFilter.getAttributes();
        }
        for (Attribute attribute : attributes) {
            SQLSelectItem item = new SQLSelectItem();
            // 对 id,a.id做分开处理
            if (owner != null) {
                SQLPropertyExpr expr = new SQLPropertyExpr();
                expr.setName(attribute.getName());
                expr.setOwner(owner);
                item.setExpr(expr);
                selectItems.add(item);
            } else {
                SQLIdentifierExpr expr = new SQLIdentifierExpr();
                expr.setName(attribute.getName());
                item.setExpr(expr);
                selectItems.add(item);
            }
        }
    }

    public void initTableFilter(SQLTableSource x) {
        if (x instanceof SQLExprTableSource) {
            tableFilter = TableManager.newTableFilter((SQLExprTableSource) x, this);
            putTableFilter(tableFilter);
        } else if (x instanceof SQLJoinTableSource) {
            tableFilter = initTableFilter((SQLJoinTableSource) x);
        } else {
            throw new RuntimeException(" not support this tableSource");
        }
    }

    // for join
    @Override
    public boolean visit(SQLJoinTableSource x) {
        tableFilter = initTableFilter(x);
        // false 表示不继续向下visit
        return false;
    }

    public TableFilter initTableFilter(SQLJoinTableSource x) {
        checkJoinType(x.getJoinType());
        SQLTableSource left = x.getLeft();
        SQLTableSource right = x.getRight();
        TableFilter leftTableFilter;
        if (left instanceof SQLExprTableSource) {
            leftTableFilter = TableManager.newTableFilter((SQLExprTableSource) left, this);
            // 构造map和tableFilter的映射
            putTableFilter(leftTableFilter);
        } else {
            leftTableFilter = initTableFilter((SQLJoinTableSource) left);
        }
        TableFilter rightTableFilter;
        if (right instanceof SQLExprTableSource) {
            rightTableFilter = TableManager.newTableFilter((SQLExprTableSource) right, this);
            // 构造map和tableFilter的映射
            putTableFilter(rightTableFilter);
        } else {
            rightTableFilter = initTableFilter((SQLJoinTableSource) right);
        }
        // 将rightTableFilter加到最深层的join为null的地方
        TableFilter tempTableFilter = leftTableFilter;
        while (tempTableFilter.getJoin() != null) {
            tempTableFilter = tempTableFilter.getJoin();
        }
        tempTableFilter.setJoin(rightTableFilter);
        tempTableFilter.setJoinCondition(x.getCondition());
        return leftTableFilter;

    }

    void putTableFilter(TableFilter tableFilter) {
        if (aliasMap.get(tableFilter.getTableAlias()) != null) {
            throw new RuntimeException("can not define two same alias");
        }
        if (tableFilter.getTableAlias() != null) {
            aliasMap.put(tableFilter.getTableAlias(), tableFilter);
        }
    }

    public void checkJoinType(SQLJoinTableSource.JoinType joinType) {
        if (!(joinType == SQLJoinTableSource.JoinType.JOIN
                      || joinType == SQLJoinTableSource.JoinType.INNER_JOIN)) {
            throw new RuntimeException("Only support inner join");
        }
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    public List<SQLSelectItem> getSelectItems() {
        return selectItems;
    }

    public SelectVisitor setSelectItems(List<SQLSelectItem> selectItems) {
        this.selectItems = selectItems;
        return this;
    }

    public SelectVisitor setTableFilter(TableFilter tableFilter) {
        this.tableFilter = tableFilter;
        return this;
    }

    public SQLExpr getWhereCondition() {
        return whereCondition;
    }

    public SelectVisitor setWhereCondition(SQLExpr whereCondition) {
        this.whereCondition = whereCondition;
        return this;
    }

    public TableFilter getTableFilter(String alias) {
        return aliasMap.get(alias);
    }

}
