package io.github.logtube.utils;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Dates {

    private static final ThreadLocal<SimpleDateFormat> LINE_TIMESTAMP_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat get() {
            return new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS ZZZZZ]");
        }
    };

    private static final ThreadLocal<SimpleDateFormat> DATE_MARK_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat get() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private static final ThreadLocal<SimpleDateFormat> ISO_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat get() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }

    };

    public static @NotNull String formatLineTimestamp(long epoch) {
        return LINE_TIMESTAMP_FORMAT.get().format(new Date(epoch));
    }

    public static @NotNull String formatDateMark(long epoch) {
        return DATE_MARK_FORMAT.get().format(new Date(epoch));
    }

    public static @NotNull String formatISO(long epoch) {
        return ISO_FORMAT.get().format(new Date(epoch));
    }

}
