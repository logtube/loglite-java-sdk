package io.github.logtube.dubbo27;

import io.github.logtube.Logtube;
import io.github.logtube.LogtubeConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.ArrayList;
import java.util.List;

@Activate(group = {"provider"})
public class LogtubeDubboProviderFilter implements Filter {

    private static final String[] TRACE_ID_PROVIDER_CLASSES = new String[]{
            "io.github.logtube.dubbo27.traceids.SkywalkingContextProvider"
    };

    private static final List<DubboTraceIdProvider> TRACE_ID_PROVIDERS;

    static {
        ArrayList<DubboTraceIdProvider> providers = new ArrayList<>();
        for (String className : TRACE_ID_PROVIDER_CLASSES) {
            try {
                Class<?> providerClass = Class.forName(className);
                Object provider = providerClass.newInstance();
                if (provider instanceof DubboTraceIdProvider) {
                    providers.add((DubboTraceIdProvider) provider);
                }
            } catch (Throwable e) {
                System.err.println("Failed to load dubbo27+ traceid provider: " + className + ": " + e.getMessage());
            }
        }
        TRACE_ID_PROVIDERS = providers;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        DubboAccessEventCommitter builder = new DubboAccessEventCommitter();
        try {
            setupRootLogger(invocation);
            return invoker.invoke(invocation);
        } finally {
            builder.commit();
            resetRootLogger();
        }
    }

    private void setupRootLogger(Invocation invocation) {
        String crid = RpcContext.getContext().getAttachment(LogtubeConstants.DUBBO_CRID_KEY);
        if (crid == null) {
            for (DubboTraceIdProvider provider : TRACE_ID_PROVIDERS) {
                crid = provider.extractTraceId(invocation);
                if (crid != null) {
                    break;
                }
            }
        }
        Logtube.getProcessor().setCrid(crid);
        Logtube.getProcessor().setCrsrc(RpcContext.getContext().getAttachment(LogtubeConstants.DUBBO_CRSRC_KEY));
        Logtube.getProcessor().setPath(RpcContext.getContext().getAttachment("interface") + "." + RpcContext.getContext().getMethodName());
    }

    private void resetRootLogger() {
        Logtube.getProcessor().clearContext();
    }

}
