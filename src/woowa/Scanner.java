package woowa;

import static woowa.TokenType.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

    private static final Map<String, TokenType> keywords;


    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("그리고", AND);

        keywords.put("class", CLASS);
        keywords.put("클래스", CLASS);

        keywords.put("else", ELSE);
        keywords.put("아니면", ELSE);

        keywords.put("false", FALSE);
        keywords.put("거짓", FALSE);

        keywords.put("for", FOR);
        keywords.put("반복", FOR);

        keywords.put("fun", FUN);
        keywords.put("함수", FUN);

        keywords.put("if", IF);
        keywords.put("만약", IF);

        keywords.put("nil", NIL);
        keywords.put("널", NIL);

        keywords.put("or", OR);
        keywords.put("또는", OR);

        keywords.put("print", PRINT);
        keywords.put("출력", PRINT);

        keywords.put("return", RETURN);
        keywords.put("반환", RETURN);

        keywords.put("super", SUPER);
        keywords.put("상위", SUPER);

        keywords.put("this", THIS);
        keywords.put("자신", THIS);

        keywords.put("true", TRUE);
        keywords.put("참", TRUE);

        keywords.put("var", VAR);
        keywords.put("변수", VAR);

        keywords.put("while", WHILE);
        keywords.put("하는동안", WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0; // 렉심의 첫 번째 문자
    private int current = 0; // 현재 처리중인 문자
    private int line = 1; // 소스 줄 번호

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // 다음 렉심의 시작부에 있다
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break; //

            // 두 문자 이상일 경우 처리
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            // / 처리 주석 or 주석 아닐 경우
            case '/':
                if (match('/')) {
                    // 주식은 줄 끝 까지 이어진다.
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // 개행 문자 무시
                break;

            case '\n':
                line++;
                break;

            // 문자열 처리
            case '"':
                string();
                break;

            default:
                // 숫자 처리
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Woowa.error(line, "예상되지 않은 특수문자.");
                    break;
                }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text); // 키워드 맵에서 찾기
        if (type == null) {
            type = IDENTIFIER; // 키워드가 아니면 식별자
        }
        addToken(IDENTIFIER);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                advance();
            }

            if (isAtEnd()) {
                Woowa.error(line, "문장이 종료되지 않았습니다.");
            }

            // 문장이 닫기면
            advance();

            // 앞 뒤 큰 따옴표 제거
            String value = source.substring(start + 1, current - 1);
            addToken(STRING, value);
        }
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            // "." 을 소비한다
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        // 숫자 부분을 double 로 변환한다
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // _로 시작되는 렉심을 모두 식별자라 가정
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            (c >= '가' && c <= '힣') || // 한글도 가능
            c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // != 와 같이 2개 짜리 문자 처리
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
