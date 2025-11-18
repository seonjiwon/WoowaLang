package function;

import java.util.List;
import woowa.Interpreter;
import woowa.RuntimeError;
import woowa.Token;
import woowa.WoowaArray;

public class ArrayPushFunction implements NativeFunction {

    @Override
    public String getName() {
        return "배열추가";
    }

    @Override
    public int arity() {
        return 2;  // 추가(배열, 값)
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object arrayObj = arguments.get(0);
        Object value = arguments.get(1);

        if (!(arrayObj instanceof WoowaArray)) {
            throw new RuntimeError(
                new Token(null, "", null, -1),
                "배열추가() 함수의 첫 번째 인자는 배열이어야 합니다."
            );
        }

        WoowaArray array = (WoowaArray) arrayObj;
        array.push(value);

        return null;
    }

    @Override
    public String toString() {
        return "<native fn>";
    }
}