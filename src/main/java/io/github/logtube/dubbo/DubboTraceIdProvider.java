package io.github.logtube.dubbo;

import com.alibaba.dubbo.rpc.Invocation;
import org.jetbrains.annotations.Nullable;

public interface DubboTraceIdProvider {

    @Nullable
    String extractTraceId(Invocation invocation);

}
