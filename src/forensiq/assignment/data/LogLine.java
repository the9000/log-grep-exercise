package forensiq.assignment.data;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Log line has a host part, a data stamp, and the rest.
 * The line is "hostname - - [timestamp tz] rest". All lines follow this pattern, except for mangled first lines.
 */
public class LogLine {
    public final String host;
    public final String rest;
    public final Date timestamp;

    // Unfortunately, the space also breaks the timestamp, too.
    private static final Pattern split_pattern = Pattern.compile(" ");
    private static final DateFormat date_format = new SimpleDateFormat("'['dd/MMM/yyyy:HH:mm:ss Z']'");

    private LogLine(String host, String rest, Date timestamp) {
        this.host = host;
        this.rest = rest;
        this.timestamp = timestamp;
    }

    public static LogLine fromLine(final String line) {
        // NOTE: this is expensive; it slowed things down 3x.
        String[] pieces = split_pattern.split(line, 6);
        if (pieces.length >= 5) {
            final String host = pieces[0];
            final String rest = pieces.length == 6 ? pieces[5] : "";
            final String timestamp_string = pieces[3] + " " + pieces[4];
            final Date timestamp = date_format.parse(timestamp_string, new ParsePosition(0));
            if (timestamp != null) {
                return new LogLine(host, rest, timestamp);
            }
        }
        return new LogLine("", line, null);
    }
}
