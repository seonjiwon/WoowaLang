package woowa;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Map<String, Object> values = new HashMap<>();

    Object get(Token name) {
        // 변수가 발견되면 해당 변수에 바인딩 된 값 return
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // 정의되지 않은 변수면 에러 발생
        throw new RuntimeError(name, "정의되지 않은 변수 '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

}
