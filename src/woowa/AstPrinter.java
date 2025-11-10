package woowa;

import java.util.List;
import woowa.Expr.Assign;
import woowa.Expr.Binary;
import woowa.Expr.Grouping;
import woowa.Expr.Literal;
import woowa.Expr.Unary;
import woowa.Expr.Variable;
import woowa.Stmt.Expression;
import woowa.Stmt.Print;
import woowa.Stmt.Var;

/**
 * AST를 읽기 쉬운 문자열로 변환하는 Visitor 구현체
 *
 * 예: (1 + 2) * 3 -> "(* (group (+ 1 2)) 3)"
 */
public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String>{

    String print(Expr expr) {
        return expr.accept(this);
    }

    String print(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(block ");

        for (Stmt statement : stmt.statements) {
            builder.append(statement.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return parenthesize(";", stmt.expression);
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        if (stmt.elseBranch == null) {
            return parenthesize2("if", stmt.condition, stmt.thenBranch);
        }

        return parenthesize2("if-else", stmt.condition, stmt.thenBranch,
            stmt.elseBranch);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer == null) {
            return parenthesize2("var", stmt.name);
        }

        return parenthesize2("var", stmt.name, "=", stmt.initializer);
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return parenthesize2("while", stmt.condition, stmt.body);
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        return parenthesize2("=", expr.name.lexeme, expr.value);
    }

    // 이항 연산 표현식 방문 (예: 1 + 2, 3 * 4)
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right); // 1 + 2 -> "(+ 1 2)"
    }

    // 그룹 표현식 방문 (예: (1 + 2))
    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression); // (1 + 2) -> "(group (+ 1 2))"
    }

    // 리터럴(값) 표현식 방문 (예: 123, "hello", true)
    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) {
            return "nil"; // null -> "nil"로
        }
        return expr.value.toString(); // 123 -> "123", "hello" -> "hello"
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    // 단항 연산 표현식 방문 (예: -5, !true)
    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right); // -123 -> "(- 123)"
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return expr.name.lexeme;
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name); // 여는 괄호와 이름
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this)); // 재귀: 각 하위 표현식 방문
        }
        builder.append(")"); // 닫는 괄호

        return builder.toString(); // parenthesize("+", Literal(1), Literal(2)) -> "(+ 1 2)"
    }

    private String parenthesize2(String name, Object... parts) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        transform(builder, parts);
        builder.append(")");

        return builder.toString();
    }

    private void transform(StringBuilder builder, Object... parts) {
        for (Object part : parts) {
            builder.append(" ");
            if (part instanceof Expr) {
                builder.append(((Expr)part).accept(this));
            } else if (part instanceof Stmt) {
                builder.append(((Stmt) part).accept(this));
            } else if (part instanceof Token) {
                builder.append(((Token) part).lexeme);
            } else if (part instanceof List) {
                transform(builder, ((List) part).toArray());
            } else {
                builder.append(part);
            }
        }
    }
}
