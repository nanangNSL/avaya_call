/**
 * Copyright 2016 Avaya Inc. <br>
 * All rights reserved. Usage of this source is bound to the terms described the file
 * AVAYA SDK EULA.pdf, included in this SDK.<br>
 * Avaya – Confidential & Proprietary. Use pursuant to your signed agreement or Avaya Policy.
 */
package io.navendra.retrofitkotlindeferred.utils;

import android.content.Context;
import android.os.FileUtils;

import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Logger facade. Can be used to log to logcat alone or to a log file as well as logcat
 */
public class Logger {
    private static String FOLDER =null;

    private static final String DEFAULT_FILE_NAME = "WebRTCReferenceClient";
    private static final int MAXIMUM_ALLOWED_BACK_UP_FILES_SIZE = 5;
    private static boolean sLogToDisk = false;
    private static Context appContext;

    private org.slf4j.Logger slf4jLogger;

    private static String sFileName = "";

    public static void setApplicationContext(Context context) {
        appContext = context;
    }
    public static Logger getLogger(String tag) {
        return new Logger(tag);
    }




    private Logger(String tag) {
//        configureLogback();
        slf4jLogger = LoggerFactory.getLogger(tag);
    }



    public void v(String message) {
        try {
            slf4jLogger.trace(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void d(String message) {
        try {
            slf4jLogger.debug(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void i(String message) {
        try {
            slf4jLogger.info(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void w(String message) {
        try {
            slf4jLogger.warn(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void w(String message, Throwable tr) {
        try {
            slf4jLogger.warn(message, tr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void e(String message) {
        try {
            slf4jLogger.error(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void e(String message, Throwable tr) {
        try {
            slf4jLogger.error(message, tr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private static void configureLogback() {
//        try {
//            // reset the default context (which may already have been initialized)
//            // since we want to reconfigure it
//            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//            lc.reset();
//
//            // add the newly created appenders to the root logger;
//            // qualify Logger to disambiguate from org.slf4j.Logger
//            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
//
//            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
//            encoder.setContext(lc);
//            encoder.setPattern("%msg%n");
//            encoder.start();
//
//            LogcatAppender logcatAppender = new LogcatAppender();
//            logcatAppender.setContext(lc);
//            logcatAppender.setEncoder(encoder);
//            logcatAppender.start();
//
//            root.addAppender(logcatAppender);
//
//            if(sLogToDisk) {
//                PatternLayoutEncoder encoderDisk = new PatternLayoutEncoder();
//                encoderDisk.setContext(lc);
//                encoderDisk.setPattern("%d{HH:mm:ss.SSS} %-5level [%logger{36}] %msg%n");
//                encoderDisk.start();
//
//                RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
//                rollingFileAppender.setAppend(true);
//                rollingFileAppender.setContext(lc);
//
//                TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
//                rollingPolicy.setFileNamePattern(sFileName + ".%d.txt");
//                rollingPolicy.setMaxHistory(MAXIMUM_ALLOWED_BACK_UP_FILES_SIZE);
//                rollingPolicy.setParent(rollingFileAppender);
//                rollingPolicy.setContext(lc);
//                rollingPolicy.start();
//
//                rollingFileAppender.setEncoder(encoderDisk);
//                rollingFileAppender.setRollingPolicy(rollingPolicy);
//                rollingFileAppender.start();
//
//                root.addAppender(rollingFileAppender);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }



}