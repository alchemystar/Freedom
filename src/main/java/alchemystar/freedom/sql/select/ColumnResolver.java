package alchemystar.freedom.sql.select;

import alchemystar.hero.meta.Column;
import alchemystar.hero.meta.value.Value;

/**
 * @Author lizhuyang
 */
public interface ColumnResolver {

    Column[] getColumns();

    Value getValue(String columnName);

    TableFilter getTableFilter();

    String getTableAlias();
}
