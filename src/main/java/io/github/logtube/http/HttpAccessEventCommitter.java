package io.github.logtube.http;

import io.github.logtube.Logtube;
import io.github.logtube.core.IMutableEvent;
import io.github.logtube.utils.Flatten;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.List;

public class HttpAccessEventCommitter {

    public static boolean RECORD_HEADERS = false;

    public static boolean RECORD_HEADERS_X = false;

    private @NotNull
    final IMutableEvent event = Logtube.getLogger(HttpAccessEventCommitter.class).topic("x-access");

    private final long startAt = System.currentTimeMillis();

    private HttpServletRequest httpRequest;

    private boolean hasResponse = false;

    @Contract("_ -> this")
    public @NotNull HttpAccessEventCommitter setServletRequest(@NotNull ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            return this;
        }
        this.httpRequest = (HttpServletRequest) request;
        this.event
                .extra("method", httpRequest.getMethod())
                .extra("host", httpRequest.getServerName())
                .extra("query", httpRequest.getQueryString())
                .extra("header_user_token", httpRequest.getHeader("UserToken"))
                .extra("header_app_info", Flatten.flattenJSON(httpRequest.getHeader("X-Defined-AppInfo")))
                .extra("header_ver_info", Flatten.flattenJSON(httpRequest.getHeader("X-Defined-VerInfo")))
                .extra("remote_addr", determineRemoteAddr(httpRequest));
        if (RECORD_HEADERS || RECORD_HEADERS_X) {
            StringBuilder stringBuilder = new StringBuilder();
            Enumeration<String> names = httpRequest.getHeaderNames();
            while (null != names && names.hasMoreElements()) {
                String name = names.nextElement();
                // 出于安全和节约日志量的考虑，忽略 cookie
                if ("cookie".equalsIgnoreCase(name)) {
                    continue;
                }
                // 如果启用了 RECORD_HEADERS_X 则只记录 X- 开头的 Header
                if (RECORD_HEADERS_X) {
                    if (!name.toLowerCase().startsWith("x-")) {
                        continue;
                    }
                }
                Enumeration<String> values = httpRequest.getHeaders(name);
                while (null != values && values.hasMoreElements()) {
                    String value = values.nextElement();
                    stringBuilder.append(name);
                    stringBuilder.append(":");
                    stringBuilder.append(value);
                    stringBuilder.append("\n");
                }
            }
            this.event.extra("headers", stringBuilder.toString());
        }
        return this;
    }

    @NotNull
    private static String determineRemoteAddr(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            String[] xffs = xff.split(",");
            if (xffs.length > 0) {
                return xffs[0].trim();
            }
        }
        String ri = request.getHeader("X-Real-IP");
        if (ri != null && !ri.isEmpty()) {
            return ri.trim();
        }
        return request.getRemoteAddr();
    }

    @Contract("_ -> this")
    public @NotNull HttpAccessEventCommitter setServletResponse(@NotNull ServletResponse response) {
        this.hasResponse = true;
        if (!(response instanceof HttpServletResponse)) {
            return this;
        }
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        this.event.extra("status", httpResponse.getStatus());

        // 设置返回值大小
        if (response instanceof LogtubeHttpServletResponseWrapper) {
            this.event.extra("response_size", ((LogtubeHttpServletResponseWrapper) response).getResponseSize());
        }
        return this;
    }

    public void commit() {
        if (!this.hasResponse) {
            this.event.extra("status", 500);
        }

        List<String> params = null;

        // 为避免调用getParameterMap()方法影响业务使用Request Body，放到过滤器退出时记录参数。
        if (this.httpRequest instanceof LogtubeHttpServletRequestWrapper) {
            params = ((LogtubeHttpServletRequestWrapper) httpRequest).getParams();
        } else {
            params = Flatten.flattenParameters(httpRequest.getParameterMap());
        }

        this.event.xDuration(System.currentTimeMillis() - this.startAt);

        if (params != null) {
            this.event.extra("params", String.join(",", params));
        }

        this.event.commit();
    }

}
