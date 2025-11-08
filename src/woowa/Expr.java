package woowa;

import java.util.List;

/**
 * Expression AST 노트의 최상위 추상 클래스
 *
 * 모든 표현식은 이 클래스를 상속받아 트리 구조를 형성
 *
 * 포함된 표현식 타입:
 * - Binary: 이항 연산 (1 + 2, 3 * 4)
 * - Grouping: 괄호 표현식 ((1 + 2))
 * - Literal: 리터럴 값 (123, "hello", true)
 * - Unary: 단항 연산 (-5, !true)
 */
abstract class Expr {

    interface Visitor<R> {

        R visitBinaryExpr(Binary expr);

        R visitGroupingExpr(Grouping expr);

        R visitLiteralExpr(Literal expr);

        R visitUnaryExpr(Unary expr);
    }

    // Nested Expr classes here...
//> expr-binary
    static class Binary extends Expr {

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    //< expr-binary
//> expr-grouping
    static class Grouping extends Expr {

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
    }

    //< expr-grouping
//> expr-literal
    static class Literal extends Expr {

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }

    //< expr-literal
//> expr-unary
    static class Unary extends Expr {

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }
//< expr-unary

    abstract <R> R accept(Visitor<R> visitor);
}
//< Appendix II expr
