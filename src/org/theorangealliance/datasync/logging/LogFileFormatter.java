package org.theorangealliance.datasync.logging;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by Kyle Flynn on 12/3/2017.
 */
public class LogFileFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        Date date = new Date();
        String timeStamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
        return "[" + timeStamp + "][" + record.getLevel().getName() + "][THREAD " + record.getThreadID() + "] " + record.getMessage() + "\n";
    }
}
