package alchemystar.freedom.engine.parser;

import alchemystar.freedom.engine.parser.util.ParseUtil;

/**
 * ServerParse
 *
 * @Author lizhuyang
 */
public final class ServerParse {

    public static int parse(String stmt) {
        for (int i = 0; i < stmt.length(); ++i) {
            switch (stmt.charAt(i)) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                case '/':
                case '#':
                    i = ParseUtil.comment(stmt, i);
                    continue;
                case 'B':
                case 'b':
                    return ServerParseCheck.beginCheck(stmt, i);
                case 'C':
                case 'c':
                    return ServerParseCheck.createSchemaAndCommit(stmt, i);
                case 'D':
                case 'd':
                    return ServerParseCheck.deleteCheck(stmt, i);
                case 'E':
                case 'e':
                    return ServerParseCheck.explainCheck(stmt, i);
                case 'I':
                case 'i':
                    return ServerParseCheck.insertCheck(stmt, i);
                case 'R':
                case 'r':
                    return ServerParseCheck.rCheck(stmt, i);
                case 'S':
                case 's':
                    return ServerParseCheck.sCheck(stmt, i);
                case 'U':
                case 'u':
                    return ServerParseCheck.uCheck(stmt, i);
                case 'K':
                case 'k':
                    return ServerParseCheck.killCheck(stmt, i);
                default:
                    return ServerParseCheck.OTHER;
            }
        }
        return ServerParseCheck.OTHER;
    }

}
