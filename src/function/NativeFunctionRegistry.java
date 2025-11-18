package function;

import java.util.List;
import woowa.Environment;

public class NativeFunctionRegistry {

    private List<NativeFunction> getNativeFunctions() {
        return List.of(
            new ClockFunction(),
            new ReadInputFunction(),
            new SplitFunction(),
            new LengthFunction(),
            new SubstringFunction(),
            new ContainsFunction(),
            new ParseIntFunction(),
            new ArraySizeFunction(),
            new ArrayPushFunction()
        );
    }

    public void registerAll(Environment globals) {
        for (NativeFunction function : getNativeFunctions()) {
//            System.out.println("함수 등록: " + function.getName());
            globals.define(function.getName(), function);
        }
    }
}
