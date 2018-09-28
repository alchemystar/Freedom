package alchemystar.freedom.engine.parser;

import alchemystar.freedom.engine.parser.util.ParseUtil;

/**
 * ServerParseStart
 * author lizhuyang
 */
public final class ServerParseStart {

    public static final int OTHER = -1;
    public static final int TRANSACTION = 1;

    public static int parse(String stmt, int offset) {
        int i = offset;
        for (; i < stmt.length(); i++) {
            switch (stmt.charAt(i)) {
                case ' ':
                    continue;
                case '/':
                case '#':
                    i = ParseUtil.comment(stmt, i);
                    continue;
                case 'T':
                case 't':
                    return transactionCheck(stmt, i);
                default:
                    return OTHER;
            }
        }
        return OTHER;
    }

    // START TRANSACTION
    static int transactionCheck(String stmt, int offset) {
        if (stmt.length() > offset + "ransaction".length()) {
            char c1 = stmt.charAt(++offset);
            char c2 = stmt.charAt(++offset);
            char c3 = stmt.charAt(++offset);
            char c4 = stmt.charAt(++offset);
            char c5 = stmt.charAt(++offset);
            char c6 = stmt.charAt(++offset);
            char c7 = stmt.charAt(++offset);
            char c8 = stmt.charAt(++offset);
            char c9 = stmt.charAt(++offset);
            char c10 = stmt.charAt(++offset);
            if ((c1 == 'R' || c1 == 'r') && (c2 == 'A' || c2 == 'a') && (c3 == 'N' || c3 == 'n')
                    && (c4 == 'S' || c4 == 's') && (c5 == 'A' || c5 == 'a') && (c6 == 'C' || c6 == 'c')
                    && (c7 == 'T' || c7 == 't') && (c8 == 'I' || c8 == 'i') && (c9 == 'O' || c9 == 'o')
                    && (c10 == 'N' || c10 == 'n')
                    && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
                return TRANSACTION;
            }
        }
        return OTHER;
    }

}