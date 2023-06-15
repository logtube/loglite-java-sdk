package io.github.logtube.http.traceids;

import io.github.logtube.LogtubeConstants;
import io.github.logtube.http.HttpTraceIdProvider;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;

public class B3TraceIdProvider implements HttpTraceIdProvider {

    @Override
    public @Nullable String extractTraceId(HttpServletRequest request) {
        return request.getHeader(LogtubeConstants.HTTP_B3_HEADER_TRACE_ID);
    }

}
