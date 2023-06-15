package io.github.logtube.http.traceids;

import io.github.logtube.http.HttpTraceIdProvider;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;

public class SkywalkingContextProvider implements HttpTraceIdProvider {

    public SkywalkingContextProvider() {
        // 强制触发类加载
        TraceContext.traceId();
    }

    @Override
    public @Nullable String extractTraceId(HttpServletRequest request) {
        String traceId = TraceContext.traceId();
        if (traceId != null) {
            if (traceId.length() < 16) {
                traceId = null;
            }
        }
        return traceId;
    }

}
