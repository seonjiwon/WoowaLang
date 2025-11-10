package woowa;

import static woowa.TokenType.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser (구문 분석기) 토큰 리스트를 AST로 변환
 * <p>
 * 연산자 우선순위 (높은 것부터): 1. primary    -> 숫자, 문자열, 괄호 2. unary      -> !, - 3. factor     -> *, / 4. term
 * -> +, - 5. comparison -> >, >=, <, <= 6. equality   -> ==, !=
 */
public class Parser {

    private static class ParseError extends RuntimeException {

    }

    private final List<Token> tokens; // 파싱할 토큰 리스트
    private int current = 0; // 현재 처리중이 토큰의 위치

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // 초기 메서드 정의
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    /**
     * 표현식 파싱 시작점 가잔 낮은 우선순위 equality 부터 시작
     */
    private Expr expression() {
        return equality();
    }

    private Stmt statement() {
        // print 토큰이 나오면 print 문
        if (match(PRINT)) {
            return printStatement();
        }

        // 알러진 문장처럼 보이지 않으면 표현문이라 가정
        return expressionStatement();
    }

    // print 문 처리
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "값 뒤에 ';'이 필요합니다.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "표현식 뒤에 ';'이 필요합니다.");
        return new Stmt.Expression(expr);
    }

    /**
     * 동등 비교 연산자 파싱(==, !=) equality -> comparison ( ( "!=" | "==" ) comparison )* ;
     */
    private Expr equality() {
        Expr expr = comparison(); // 먼저 더 높은 우선순위 파싱

        // ==, != 연산자가 계속 나오는 동안 반복
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous(); // 방금 매치된 연산자 가져오기
            Expr right = comparison(); // 오른쪽 피연산자 파싱
            expr = new Expr.Binary(expr, operator, right); // 왼쪽 결과를 새로운 Binary 노드로 감싸기
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }


    /**
     * 가장 높은 우선순위의 표현식 파싱 (리터럴, 괄호)
     * primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
     */
    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }

        if (match(TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        // previous().literal: 토큰의 실제 값
        // 예: "123" 토큰 -> 123.0 (double)
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        // 괄호로 묶인 표현식 (그룹)
        if (match(LEFT_PAREN)) {
            Expr expr = expression(); // 괄호 안의 표현식 재귀 파싱
            consume(RIGHT_PAREN, "표현식 다음에 ')'가 필요합니다."); // ')' 필수!
            return new Expr.Grouping(expr); // (1 + 2) -> Grouping(Binary(1, +, 2))
        }

        // 어떤 것도 매치되지 않으면 에러
        // "+ 5" -> +는 primary 가 될 수 없음
        throw error(peek(), "표현식이 필요합니다.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            // 토큰이 주어진 타입 중 하나라도 해당하는지 확인
            if (check(type)) {
                // 주어진 타입 중 하나라면 토큰 소비 후 return
                advance();
                return true;
            }
        }
        // 현재 토큰 유지
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Woowa.error(token, message);
        return new ParseError();
    }

    /**
     * 파서 동기화
     * 에러 발생 후 파서를 복구하여 나머지 코드도 계속 파실 할 수 있게 함
     *
     * 하나의 에러 때문에 전체 파싱을 중단하지 않고,
     * 다음 문장의 시작점 까지 건너 뛰어 계속 진행
     */
    private void synchronize() {
        advance(); // 에러 난 토큰 건너 뛰기

        while (!isAtEnd()) {
            // 세미 콜론을 만나면 문장의 끝
            if (previous().type == SEMICOLON) return;

            // 다음 토큰이 새로운 문장의 시작이면 복구 완료
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
