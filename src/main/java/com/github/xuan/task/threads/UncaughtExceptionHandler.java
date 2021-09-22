package com.github.xuan.task.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UncaughtExceptionHandler.class);

    static Thread.UncaughtExceptionHandler getInstance() {
        return new UncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOG.error("thread {} com.wujie.cxyx error exit", t.getName(), e);
    }
}
