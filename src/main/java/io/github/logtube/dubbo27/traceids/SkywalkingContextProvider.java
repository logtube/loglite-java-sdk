package io.github.logtube.dubbo27.traceids;

import io.github.logtube.dubbo27.DubboTraceIdProvider;
import org.apache.dubbo.rpc.Invocation;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.jetbrains.annotations.Nullable;

public class SkywalkingContextProvider implements DubboTraceIdProvider {

    public SkywalkingContextProvider() {
        // 强制触发类加载
        TraceContext.traceId();
    }

    @Override
    public @Nullable String extractTraceId(Invocation invocation) {
        String traceId = TraceContext.traceId();
        if (traceId != null && traceId.equals("")) {
            traceId = null;
        }
        return traceId;
    }

}
