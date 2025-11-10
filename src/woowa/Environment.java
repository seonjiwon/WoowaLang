package woowa;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        // 변수가 발견되면 해당 변수에 바인딩 된 값 return
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // 변수가 발결되지 않으면 주변 환경을 재귀적으로 탐색
        if (enclosing != null) {
            return enclosing.get(name);
        }

        // 정의되지 않은 변수면 에러 발생
        throw new RuntimeError(name, "정의되지 않은 변수 '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "정의되지 않은 변수 '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

}
