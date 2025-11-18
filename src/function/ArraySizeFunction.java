package function;

import java.util.List;
import woowa.Interpreter;
import woowa.RuntimeError;
import woowa.Token;
import woowa.WoowaArray;

public class ArraySizeFunction implements NativeFunction {

    @Override
    public String getName() {
        return "배열크기";
    }

    @Override
    public int arity() {
        return 1;  // 배열크기(배열)
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Object arrayObj = arguments.get(0);

        if (!(arrayObj instanceof WoowaArray)) {
            throw new RuntimeError(
                new Token(null, "", null, -1),
                "배열크기() 함수는 배열을 인자로 받아야 합니다."
            );
        }

        WoowaArray array = (WoowaArray) arrayObj;
        return (double) array.size();
    }

    @Override
    public String toString() {
        return "<native fn 크기>";
    }
}