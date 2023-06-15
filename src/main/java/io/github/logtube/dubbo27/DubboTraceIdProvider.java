package io.github.logtube.dubbo27;

import org.apache.dubbo.rpc.Invocation;
import org.jetbrains.annotations.Nullable;

public interface DubboTraceIdProvider {

    @Nullable
    String extractTraceId(Invocation invocation);

}
