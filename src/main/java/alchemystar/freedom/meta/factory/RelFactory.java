package alchemystar.freedom.meta.factory;

import alchemystar.freedom.config.SystemConfig;
import alchemystar.freedom.meta.Relation;

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

    public Relation newRelation(String tableName) {
        Relation relation = new Relation();
        relation.setRelPath(SystemConfig.RELATION_FILE_PRE_FIX + tableName);
        relation.setMetaPath(SystemConfig.RELATION_FILE_PRE_FIX + tableName + "_meta");
        return relation;
    }
}
