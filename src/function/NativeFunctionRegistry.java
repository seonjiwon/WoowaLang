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
            new ParseIntFunction()
        );
    }

    public void registerAll(Environment globals) {
//        System.out.println("Native Functions 등록 중..."); // 등록 확인용 메시지
        for (NativeFunction function : getNativeFunctions()) {
            globals.define(function.getName(), function);
//            System.out.println("  -> '" + function.getName() + "' 등록 완료.");
        }
    }
}
