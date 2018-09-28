package alchemystar.freedom.sql;

import java.util.List;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;

import alchemystar.freedom.engine.net.handler.frontend.FrontendConnection;
import alchemystar.freedom.engine.net.proto.util.Fields;
import alchemystar.freedom.engine.net.response.SelectResponse;
import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueBoolean;
import alchemystar.freedom.sql.parser.SelectVisitor;
import alchemystar.freedom.sql.select.TableFilter;
import alchemystar.freedom.sql.select.item.SelectExprEval;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Author lizhuyang
 */
public class SelectExecutor {

    private SQLStatement sqlStatement;
    private SelectVisitor selectVisitor;
    private FrontendConnection con;
    private boolean isFieldWrite;

    public SelectExecutor(SQLStatement sqlStatement, FrontendConnection con) {
        this.sqlStatement = sqlStatement;
        this.con = con;
        isFieldWrite = false;

    }

    public void execute() {
        init();
        TableFilter tableFilter = selectVisitor.getTableFilter();
        List<SQLSelectItem> items = selectVisitor.getSelectItems();
        SelectResponse selectResponse = new SelectResponse(items.size());
        // 获取buffer
        ByteBuf buffer = null;
        if (con != null) {
            ChannelHandlerContext ctx = con.getCtx();
            buffer = ctx.alloc().buffer();
        }

        while (tableFilter.next()) {
            if (checkWhereCondition()) {
                Value[] row = new Value[items.size()];
                for (int i = 0; i < items.size(); i++) {
                    SQLSelectItem item = items.get(i);
                    SelectExprEval itemEval = new SelectExprEval(item.getExpr(), selectVisitor);
                    row[i] = itemEval.eval();
                    System.out.print(row[i].toString() + ",");
                }
                System.out.println();
                // 查找出第一个之后再计算type
                if (!isFieldWrite) {
                    if (con != null) {
                        // field
                        writeFields(items, row, con, selectResponse, buffer);
                        // eof
                        selectResponse.writeEof(con, buffer);
                        isFieldWrite = true;
                    }
                }
                // rows
                if (con != null) {
                    selectResponse.writeRow(row, con, buffer);
                }
            }
        }
        // 如果不行,就直接用string做type
        if (con != null) {
            if (!isFieldWrite) {
                // field
                writeFields(items, null, con, selectResponse, buffer);
                // eof
                selectResponse.writeEof(con, buffer);
                isFieldWrite = true;
            }
        }
        // lastEof
        if (con != null) {
            selectResponse.writeLastEof(con, buffer);
        }
    }

    public void writeFields(List<SQLSelectItem> items, Value[] values, FrontendConnection con, SelectResponse
            selectResponse, ByteBuf buffer) {
        for (int i = 0; i < items.size(); i++) {
            // 默认是string
            int type = Fields.FIELD_TYPE_STRING;
            if (values != null && values[i] != null) {
                type = SelectResponse.convertValueTypeToFieldType(values[i].getType());
            }
            String fieldName = items.get(i).toString();
            selectResponse.addField(fieldName, type);
        }
        selectResponse.responseFields(con, buffer);
    }

    private boolean checkWhereCondition() {
        // 如果没有where condition,则直接返回true
        if (selectVisitor.getWhereCondition() == null) {
            return true;
        }
        SelectExprEval eval = new SelectExprEval(selectVisitor.getWhereCondition(), selectVisitor);
        Value value = eval.eval();
        if (value instanceof ValueBoolean) {
            return ((ValueBoolean) value).getBoolean();
        }
        throw new RuntimeException("where condition eval not boolean , wrong");
    }

    public void init() {
        SelectVisitor selectVisitor = new SelectVisitor();
        sqlStatement.accept(selectVisitor);
        this.selectVisitor = selectVisitor;
    }
}
