package woowa;

enum TokenType {
    // 단일 문자 토큰 (한 글자로 이루어진 연산자나 구분자)
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // 1개 또는 2개 문자로 이루어진 토큰 (비교 연산자 등)
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // 리터럴 (값을 나타내는 토큰)
    IDENTIFIER, STRING, NUMBER,

    // 예약 키워드 (언어의 특별한 의미를 가진 단어들)
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    EOF
}
