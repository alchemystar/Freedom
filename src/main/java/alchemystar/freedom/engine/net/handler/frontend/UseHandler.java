package alchemystar.freedom.engine.net.handler.frontend;

/**
 * UseHandler
 *
 * @Author lizhuyang
 */
public final class UseHandler {

    public static void handle(String sql, FrontendConnection c, int offset) {
        String schema = sql.substring(offset).trim();
        int length = schema.length();
        if (length > 0) {
            if (schema.charAt(0) == '`' && schema.charAt(length - 1) == '`') {
                schema = schema.substring(1, length - 2);
            }
        }

        // 表示当前连接已经指定了schema
        if (c.getSchema() != null) {
            if (c.getSchema().equals(schema)) {
                c.writeOk();
            } else {
                c.schema = schema;
                c.getSession().changeCurrentSchema(schema);
                c.writeOk();
            }
            return;
        }
        c.setSchema(schema);
        c.getSession().changeCurrentSchema(schema);
        c.writeOk();

    }

}