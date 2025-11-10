package woowa;

import woowa.Expr.Binary;
import woowa.Expr.Grouping;
import woowa.Expr.Literal;
import woowa.Expr.Unary;
import woowa.Expr.Variable;

/**
 * AST를 읽기 쉬운 문자열로 변환하는 Visitor 구현체
 *
 * 예: (1 + 2) * 3 -> "(* (group (+ 1 2)) 3)"
 */
public class AstPrinter implements Expr.Visitor<String>{

    String print(Expr expr) {
        return expr.accept(this);
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
}
