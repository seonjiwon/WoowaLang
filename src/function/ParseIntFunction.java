package function;

import java.util.List;
import woowa.Interpreter;
import woowa.RuntimeError;
import woowa.Token;

public class ParseIntFunction implements NativeFunction{

    @Override
    public String getName() {
        return "정수파싱";
    }

    @Override
    public int arity() {
        return 1; // 정수파싱(문자열)
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object arg0 = arguments.get(0);

        if (!(arg0 instanceof String)) {
            throw new RuntimeError(new Token(null, "", null, -1),
                "정수파싱()의 인자는 문자열이어야 합니다.");
        }

        String target = (String) arg0;

        try {
            int value = Integer.parseInt(target);
            return (double) value;
        } catch (NumberFormatException e) {
            throw new RuntimeError(new Token(null, "", null, -1),
                "문자열 '" + target + "'을 정수로 변환할 수 없습니다.");
        }
    }

    @Override
    public String toString() {
        return "<native fn parseInt>";
    }
}
