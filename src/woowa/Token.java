package woowa;

public class Token {

    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line; // 토큰이 위치한 줄 번호

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
