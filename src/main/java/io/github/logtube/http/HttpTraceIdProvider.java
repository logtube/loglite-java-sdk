package io.github.logtube.http;


import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;

public interface HttpTraceIdProvider {

    @Nullable
    String extractTraceId(HttpServletRequest request);

}
