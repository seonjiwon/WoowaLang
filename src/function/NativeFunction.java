package function;

import woowa.WoowaCallable;

public interface NativeFunction extends WoowaCallable {

    // 함수에서 사용될 언어 반환
    String getName();
}
