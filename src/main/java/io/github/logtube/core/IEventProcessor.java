package io.github.logtube.core;

import io.github.logtube.utils.ILifeCycle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 处理器暴露的接口，跟日志器负责存储 主机名，项目名，环境名 和 当前线程的 CRID，负责产生具有有效 commit 方法的日志事件，内部保持一组输出
 */
public interface IEventProcessor extends ILifeCycle {

    @NotNull
    IEventContext captureContext();

    void clearContext();

    @NotNull IMutableEvent event();

    @NotNull String getProject();

    void setCrid(@Nullable String crid);

    void setCrsrc(@Nullable String crsrc);

    void setPath(@Nullable String path);

    @NotNull String getCrid();

    @NotNull String getCrsrc();

    @Nullable String getPath();

}
