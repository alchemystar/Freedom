package alchemystar.freedom.engine;

import alchemystar.freedom.meta.TableLoader;
import alchemystar.freedom.store.log.LogStore;

/**
 * @Author lizhuyang
 */
public class Database {

    private static Database database = null;
    // 默认端口号是8090
    private int serverPort = 8090;
    // 默认用户名密码是pay|miracle
    private String userName = "pay";
    private String passWd = "MiraCle";
    private TableLoader tableLoader;
    private LogStore logStore;

    // 单例模式
    static {
        database = new Database();
        // 加载数据
        TableLoader tableLoader = new TableLoader();
        tableLoader.readAllTable();
        database.setTableLoader(tableLoader);
        database.setLogStore(new LogStore());
    }

    public static Database getInstance() {
        return database;
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

    public TableLoader getTableLoader() {
        return tableLoader;
    }

    public void setTableLoader(TableLoader tableLoader) {
        this.tableLoader = tableLoader;
    }

    public LogStore getLogStore() {
        return logStore;
    }

    public void setLogStore(LogStore logStore) {
        this.logStore = logStore;
    }
}
