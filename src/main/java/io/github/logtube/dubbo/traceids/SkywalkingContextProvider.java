package io.github.logtube.dubbo.traceids;

import com.alibaba.dubbo.rpc.Invocation;
import io.github.logtube.dubbo.DubboTraceIdProvider;
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
