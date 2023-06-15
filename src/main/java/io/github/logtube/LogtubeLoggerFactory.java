package io.github.logtube;

import io.github.logtube.core.IEventLogger;
import io.github.logtube.core.IEventLoggerFactory;
import io.github.logtube.core.IEventProcessor;
import io.github.logtube.core.loggers.EventLogger;
import io.github.logtube.core.outputs.EventConsoleOutput;
import io.github.logtube.core.processors.EventProcessor;
import io.github.logtube.core.processors.NOPProcessor;
import io.github.logtube.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LogtubeLoggerFactory extends LifeCycle implements ILoggerFactory, IEventLoggerFactory, Reloadable {

    ///////////////////////  SINGLETON ////////////////////////////

    private static final LogtubeLoggerFactory SINGLETON;

    static {
        SINGLETON = new LogtubeLoggerFactory();
        SINGLETON.start();
    }

    public static LogtubeLoggerFactory getSingleton() {
        return SINGLETON;
    }

    ///////////////////////  LOGGERS ////////////////////////

    @NotNull
    private IEventProcessor processor = NOPProcessor.getSingleton();

    @NotNull
    private LogtubeOptions options = LogtubeOptions.getDefault();

    @NotNull
    private ITopicMutableAware rootTopics = new TopicAware();

    private Map<String, ITopicAware> customTopics = new HashMap<>();

    private final ConcurrentMap<String, IEventLogger> loggers = new ConcurrentHashMap<>();

    private LogtubeLoggerFactory() {
    }

    @Override
    public @NotNull ITopicAware getTopicAware(@NotNull String name) {
        ITopicAware rootTopics = this.rootTopics;
        Map<String, ITopicAware> customTopics = this.customTopics;
        if (name.equals(Logger.ROOT_LOGGER_NAME)) {
            return rootTopics;
        }
        String found = null;
        ITopicAware topics = null;
        for (Map.Entry<String, ITopicAware> entry : customTopics.entrySet()) {
            String prefix = entry.getKey();
            ITopicAware value = entry.getValue();
            if (name.toLowerCase().startsWith(prefix)) {
                if (found == null) {
                    found = prefix;
                    topics = value;
                } else if (prefix.length() > found.length()) {
                    found = prefix;
                    topics = value;
                }
            }
        }
        if (topics == null) {
            return rootTopics;
        }
        return topics;
    }

    @Override
    @NotNull
    public IEventProcessor getProcessor() {
        return this.processor;
    }

    @NotNull
    public LogtubeOptions getOptions() {
        return options;
    }

    @NotNull
    public IEventLogger getEventLogger(@Nullable String name) {
        if (name == null) {
            name = Logger.ROOT_LOGGER_NAME;
        }
        IEventLogger logger = this.loggers.get(name);
        if (logger != null) {
            return logger;
        } else {
            IEventLogger newInstance = new EventLogger(this, name);
            IEventLogger oldInstance = this.loggers.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }

    @Override
    public Logger getLogger(String name) {
        return getEventLogger(name);
    }

    private void swapProcessor(IEventProcessor newProcessor) {
        if (newProcessor == this.processor) {
            return;
        }

        newProcessor.start();
        IEventProcessor processor = this.processor;
        this.processor = newProcessor;
        processor.stop();
    }

    private void reloadLoggers() {
        this.loggers.forEach((name, l) -> {
            if (l instanceof Reloadable) {
                Reloadable r = (Reloadable) l;
                r.reload();
            }
        });
    }

    private void init() {
        ITopicMutableAware rootTopics = new TopicAware();
        Map<String, ITopicAware> customTopics = new HashMap<>();

        LogtubeOptions options = LogtubeOptions.fromClasspath();

        // setup topics
        rootTopics.setTopics(options.getTopics());

        options.getCustomTopics().forEach((k, v) -> {
            TopicAware topicAware = new TopicAware();
            topicAware.setTopics(v);
            customTopics.put(k, topicAware);
        });

        // setup processor
        EventProcessor processor = new EventProcessor();

        processor.setHostname(LogtubeOptions.getHostname());
        processor.setProject(options.getProject());
        processor.setEnv(options.getEnv());
        processor.setTopicMappings(options.getTopicMappings());

        // console output is the only output
        EventConsoleOutput output = new EventConsoleOutput();
        output.setTopics(null);
        output.setPretty(options.getConsolePretty());

        processor.addOutput(output);

        // 使用动态配置的方式，防止显式 import 而项目不需要该功能，没有配置对应的依赖库导致整体崩溃
        configureComponent("io.github.logtube.redis.LogtubeJedisConfigurator", options);
        configureComponent("io.github.logtube.http.LogtubeHttpConfigurator", options);

        this.rootTopics = rootTopics;
        this.customTopics = customTopics;
        this.options = options;

        this.swapProcessor(processor);
        this.reloadLoggers();
    }

    private void configureComponent(String className, LogtubeOptions options) {
        try {
            Class<?> clazz = Class.forName(className);
            Object object = clazz.newInstance();
            if (object instanceof LogtubeComponentConfigurator) {
                ((LogtubeComponentConfigurator) object).configure(options);
            }
        } catch (Throwable e) {
            System.err.println("Failed to load component: " + className + ": " + e.getMessage());
        }
    }

    @Override
    public synchronized void reload() {
        init();

        this.getEventLogger(null)
                .topic(LogtubeConstants.TOPIC_LIFECYCLE)
                .xLifecycle(LogtubeConstants.LIFECYCLE_LOGTUBE_RELOAD)
                .xLogtubeVersion(LogtubeConstants.VERSION)
                .commit();
    }

    public synchronized void doStart() {
        super.doStart();
        init();

        this.getEventLogger(null)
                .topic(LogtubeConstants.TOPIC_LIFECYCLE)
                .xLifecycle(LogtubeConstants.LIFECYCLE_BOOT)
                .xLogtubeVersion(LogtubeConstants.VERSION)
                .commit();
    }

    @Override
    public synchronized void doStop() {
        this.rootTopics = new TopicAware();
        this.customTopics = new HashMap<>();
        this.options = LogtubeOptions.getDefault();
        this.swapProcessor(NOPProcessor.getSingleton());
        this.reloadLoggers();
        super.doStop();
    }

}
