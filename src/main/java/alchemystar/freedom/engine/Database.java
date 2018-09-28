package alchemystar.freedom.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alchemystar.engine.loader.TableConfig;
import alchemystar.engine.loader.XmlServerLoader;
import alchemystar.parser.Parser;
import alchemystar.parser.ddl.CreateTable;
import alchemystar.schema.Schema;
import alchemystar.table.MetaTable;
import alchemystar.table.Table;
import alchemystar.util.BitField;

/**
 * @Author lizhuyang
 */
public class Database {

    private HashMap<String, Schema> schemas = new HashMap<String, Schema>();

    private final BitField objectIds = new BitField();
    // For the mybatis settings
    private Schema infoSchema;

    private static Database database = null;

    private volatile boolean metaTablesInitialized;
    // 默认端口号是8090
    private int serverPort = 8090;
    // 默认用户名密码是pay|miracle
    private String userName = "pay";
    private String passWd = "MiraCle";

    // 单例模式
    static {
        database = new Database();
        Schema infoSchema = new Schema(true, database, "information_schema");
        database.addSchema(infoSchema);
        database.setInfoSchema(infoSchema);
        database.loadConfig();
    }

    public void loadConfig() {
        Session firstSession = this.getSession();
        XmlServerLoader serverLoader = new XmlServerLoader();
        serverLoader.init();
        Map<String, List<TableConfig>> map = serverLoader.getTableConfigs();
        for (String key : map.keySet()) {
            List<TableConfig> tables = map.get(key);
            Schema schema = new Schema(false, this, key);
            for (TableConfig item : tables) {
                addTableFromConfig(schema, item, firstSession);
            }
            this.addSchema(schema);
        }
        serverPort = serverLoader.getServerPort();
        userName = serverLoader.getUserName();
        passWd = serverLoader.getPasswd();
    }

    public void reload() {
        HashMap<String, Schema> schemas = new HashMap<String, Schema>();
        schemas.put("information_schema", infoSchema);
        Session reloadSession = this.getSession();
        XmlServerLoader serverLoader = new XmlServerLoader();
        serverLoader.init();
        Map<String, List<TableConfig>> map = serverLoader.getTableConfigs();
        for (String key : map.keySet()) {
            List<TableConfig> tables = map.get(key);
            Schema schema = new Schema(false, this, key);
            for (TableConfig item : tables) {
                addTableFromConfig(schema, item, reloadSession);
            }
            schemas.put(schema.getName(), schema);
        }
        this.schemas = schemas;
    }

    private void addTableFromConfig(Schema schema, TableConfig tableConfig, Session session) {
        Parser parser = new Parser(session);
        CreateTable prepared = (CreateTable) parser.parse(tableConfig.getSql());
        prepared.setSchema(schema);
        prepared.update(tableConfig);
    }

    public ArrayList<Table> getAllTablesAndViews(boolean includeMeta) {
        if (includeMeta) {
            initMetaTables();
        }
        ArrayList<Table> list = new ArrayList<Table>();
        for (Schema schema : schemas.values()) {
            list.addAll(schema.getAllTablesAndViews());
        }
        return list;
    }

    private void initMetaTables() {
        if (metaTablesInitialized) {
            return;
        }
        synchronized(infoSchema) {
            if (!metaTablesInitialized) {
                for (int type = 0, count = MetaTable.getMetaTableTypeCount(); type < count; type++) {
                    MetaTable m = new MetaTable(infoSchema, -1 - type, type);
                    infoSchema.addTable(m);
                }
                metaTablesInitialized = true;
            }
        }
    }

    public static Database getInstance() {
        return database;
    }

    public static Session getSession() {
        Session session = new Session(database, "alchemystar", database.allocateObjectId());
        // todo CurrentSchema
        // 用test做CurrentSchema
        session.setCurrentSchema(database.findSchema("information_schema"));
        return session;

    }

    public Schema findSchema(String schemaName) {
        Schema schema = schemas.get(schemaName);
        if (schema == null) {
            throw new RuntimeException("No such database:" + schemaName);
        }
        return schema;
    }

    public void addSchema(Schema schema) {
        synchronized(this) {
            if (schemas.get(schema.getName()) != null) {
                throw new RuntimeException("Duplicated Schema:" + schema.getName());
            }
            schemas.put(schema.getName(), schema);
        }
    }

    public void addSchema(String schemaName) {
        synchronized(this) {
            if (schemas.get(schemaName) != null) {
                throw new RuntimeException("Duplicated Schema:" + schemaName);
            }
            Schema schema = new Schema(false, this, schemaName);
            schemas.put(schemaName, schema);
        }
    }

    public void addTable(Table table) {
        table.getSchema().addTable(table);
    }

    public void coverTable(Table table) {
        table.getSchema().coverTable(table);
    }

    public synchronized int allocateObjectId() {
        int i = objectIds.nextClearBit(0);
        objectIds.set(i);
        return i;
    }

    public Schema getInfoSchema() {
        return infoSchema;
    }

    public void setInfoSchema(Schema infoSchema) {
        this.infoSchema = infoSchema;
    }

    public HashMap<String, Schema> getSchemas() {
        return schemas;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWd() {
        return passWd;
    }

    public void setPassWd(String passWd) {
        this.passWd = passWd;
    }
}
