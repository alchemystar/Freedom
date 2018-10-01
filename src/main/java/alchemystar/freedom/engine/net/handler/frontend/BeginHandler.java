package alchemystar.freedom.engine.net.handler.frontend;

/**
 * BeginHandler
 *
 * @Author lizhuyang
 */
public final class BeginHandler {

    public static void handle(String stmt, FrontendConnection c) {
        c.commit();
        c.writeOk();
    }

}