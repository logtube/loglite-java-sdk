package io.github.logtube.core;

import io.github.logtube.Logtube;
import io.github.logtube.audit.XAudit;
import io.github.logtube.job.XJob;
import io.github.logtube.job.XJobCommitter;
import io.github.logtube.perf.XPerf;
import io.github.logtube.perf.XPerfCommitter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LogtubeTest {

    @Test
    public void binding() throws InterruptedException {
        Logtube.getProcessor().setCrid(null);
        Logger logger = LoggerFactory.getLogger(LogtubeTest.class);
        logger.info("hello world");
        logger.warn("warn test");
        logger.trace("hello world {}", "222");
        logger.error("err test {}", "333");
        logger.error("test exception", new IOException("my exception"));
    }

    @Test
    public void perfAndAudit() throws InterruptedException {
        IEventLogger logger = Logtube.getLogger(LogtubeTest.class);
        XPerfCommitter committer = XPerf.create(logger).setAction("hello").setActionDetail("world");
        Thread.sleep(2000);
        committer.setValueInteger(999).commit();
        XAudit.create(logger).setUserCode("100086").setUserName("郭德纲").setAction("hello").commit();
    }


    @Test
    public void multiThread() throws InterruptedException {
        Logtube.getProcessor().setCrid(null);
        Logger logger = LoggerFactory.getLogger(LogtubeTest.class);
        logger.info("hello world");
        Thread thread = new Thread(() -> logger.info("hello world from child thread"));
        thread.start();
    }

    @Test
    public void job() throws Exception {
        Logtube.getProcessor().setCrid(null);
        IEventLogger logger = Logtube.getLogger(LogtubeTest.class);
        logger.info().keyword("hello,world").message("extra message").commit();
        XJobCommitter committer = XJob.create(logger, "something_job").markStart();
        Thread.sleep(1000);
        committer.markEnd().setResult(true, "hello world").commit();
    }

}
