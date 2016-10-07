package forensiq.assignment.search;

import java.util.Date;

/**
 * A single search hit.
 */
public class Hit {
    // Public members are fine here since they are immutable.
    /** The name of the file. */
    public final String file_name;
    public final String line;
    public final long line_number;
    public final int start;
    public final int end;
    private final Date timestamp;

    Hit(String file_name, String line, int line_number, int start, int end, Date timestamp) {
        this.file_name = file_name;
        this.line = line;
        this.line_number = line_number;
        this.start = start;
        this.end = end;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Hit{" +
                "file " + file_name + '\'' +
                ", line " + line_number +
                ", " + start +
                " to " + end +
                " on " + (timestamp == null? "not set" : timestamp.toString()) +
                " in " + line +
                '}';
    }
}


