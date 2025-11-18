package function;

import java.util.List;
import woowa.Interpreter;
import woowa.RuntimeError;
import woowa.Token;

public class SubstringFunction implements NativeFunction {


    @Override
    public String getName() {
        return "부분문자열";
    }

    @Override
    public int arity() {
        return 3; // 부분문자열(문자열, 시작인덱스, 끝 인덱스)
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object targetObj = arguments.get(0);
        Object startObj = arguments.get(1);
        Object endObj = arguments.get(2);

        if (!(targetObj instanceof String) || !(startObj instanceof Double) || !(endObj instanceof Double)) {
            throw new RuntimeError(new Token(null, "", null, -1),
                "부분문자열() 인자 타입이 올바르지 않습니다. (문자열, 숫자, 숫자)가 필요합니다.");
        }

        String target = (String) targetObj;
        int start = ((Double) startObj).intValue();
        int end = ((Double) endObj).intValue();

        // 인덱스 범위 확인
        if (start < 0 || end > target.length() || start > end) {
            throw new RuntimeError(new Token(null, "", null, -1),
                "부분문자열() 인덱스 범위가 올바르지 않습니다.");
        }

        return target.substring(start, end);
    }

    @Override
    public String toString() {
        return "<native fn>";
    }
}
