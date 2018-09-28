package alchemystar.freedom.meta.factory;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.meta.Table;

/**
 * RelFactory
 *
 * @Author lizhuyang
 */
public class RelFactory {

    private static RelFactory relFactory;

    static {
        relFactory = new RelFactory();
    }

    public static RelFactory getInstance() {
        return relFactory;
    }

    public Table newRelation(String tableName) {
        Table table = new Table();
        return table;
    }
}
