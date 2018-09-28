package alchemystar.freedom.engine;

/**
 * @Author lizhuyang
 */
public class Database {

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
}
