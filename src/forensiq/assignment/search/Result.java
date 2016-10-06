package forensiq.assignment.search;

import java.util.List;

/**
 * A colection of per-file search hits.
 */
public class Result {
    // Public members are fine here and bleow since they are immutable.
    /** The name of the file. */
    public final String file_name;
    /** A read-only list of hits inside the file. */
    public final List<LinePosition> hits;

    Result(String file_name, List<LinePosition> hits) {
        this.file_name = file_name;
        this.hits = hits;
    }

    public static class LinePosition {
        public final long line_number;
        public final int line_offset;
        // We could keep file_offset, too, useful in other scenarios.
        LinePosition(long line_number, int line_offset) {
            this.line_number = line_number;
            this.line_offset = line_offset;
        }
    }
}


