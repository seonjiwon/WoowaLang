package woowa;

import static woowa.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import woowa.Expr.Variable;

/**
 * Parser (구문 분석기) 토큰 리스트를 AST로 변환
 * <p>
 * 연산자 우선순위 (높은 것부터): 1. primary    -> 숫자, 문자열, 괄호 2. unary      -> !, - 3. factor     -> *, / 4.
 * term -> +, - 5. comparison -> >, >=, <, <= 6. equality   -> ==, !=
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
            statements.add(declaration());
        }

        return statements;
    }

    /**
     * 표현식 파싱 시작점 가잔 낮은 우선순위 equality 부터 시작
     */
    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            // 변수를 선언하고 있는지 먼저 확인
            if (match(VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(IF)) {
            return ifStatement();
        }
        // print 토큰이 나오면 print 문
        if (match(PRINT)) {
            return printStatement();
        }

        if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        // 알러진 문장처럼 보이지 않으면 표현문이라 가정
        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "'if' 뒤에는 '('가 필요합니다.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "'if' 문 뒤에는 ')'가 필요합니다.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        // else 키워드가 있으면 else 절로 인식
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // print 문 처리
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "값 뒤에 ';'이 필요합니다.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "변수 이름이 필요합니다.");

        // 초기식이 없으면 Null
        Expr initializer = null;

        // 초기식이 있으면 파싱
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "변수 선언 뒤에 ';' 이 필요합니다.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "표현식 뒤에 ';'이 필요합니다.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "블럭 뒤에는 '}' 이 필요합니다.");
        return statements;
    }

    /**
     * 할당 표현식 파싱 (Assignment Expression)
     * 문법: assignment -> IDENTIFIER "=" assignment | equality ;
     *
     * 할당은 우결합(right-associative) 연산자
     * 예: a = b = c = 5; → a = (b = (c = 5))
     *
     * 예시:
     * - x = 10;        → Assign(x, 10)
     * - x = y = 5;     → Assign(x, Assign(y, 5))
     * - x + 1 = 10;    → 에러: "잘못된 할당 대상입니다."
     */
    private Expr assignment() {
        // equality 표현식으로 파싱 (좌변이 될 수 있는 표현식)
        Expr expr = or();

        // "=" 토큰이 있으면 할당으로 판단
        if (match(EQUAL)) {
            Token equals = previous();
            // 우번을 재귀적으로 할당
            Expr value = assignment();

            // 좌변이 변수인지 검증
            if (expr instanceof Expr.Variable) {
                Token name = ((Variable) expr).name;
                // Assign 노드 생성후 반환
                return new Expr.Assign(name, value);
            }

            error(equals, "잘못된 할당 대상입니다.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
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
     * 가장 높은 우선순위의 표현식 파싱 (리터럴, 괄호) primary -> NUMBER | STRING | "true" | "false" | "nil" | "("
     * expression ")" ;
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

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
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
     * 파서 동기화 에러 발생 후 파서를 복구하여 나머지 코드도 계속 파실 할 수 있게 함
     * <p>
     * 하나의 에러 때문에 전체 파싱을 중단하지 않고, 다음 문장의 시작점 까지 건너 뛰어 계속 진행
     */
    private void synchronize() {
        advance(); // 에러 난 토큰 건너 뛰기

        while (!isAtEnd()) {
            // 세미 콜론을 만나면 문장의 끝
            if (previous().type == SEMICOLON) {
                return;
            }

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
