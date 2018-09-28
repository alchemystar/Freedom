package alchemystar.freedom.sql.select;

import alchemystar.freedom.meta.Attribute;
import alchemystar.freedom.meta.value.Value;

/**
 * @Author lizhuyang
 */
public interface ColumnResolver {

    Attribute[] getAttributes();

    Value getValue(String columnName);

    TableFilter getTableFilter();

    String getTableAlias();
}
