import forensiq.assignment.data.Either;
import forensiq.assignment.data.LogLine;
import forensiq.assignment.search.Hit;
import forensiq.assignment.search.SearchInFile;
import forensiq.assignment.search.TreeWalk;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static forensiq.assignment.data.Either.fail;

/**
 * Command-line entry point.
 */
public class Main {

    private static final String[] help_text = {
            "Usage: <prog> [before <timestamp>] [after <timestamp>] regexp directory_name",
            "",
            "  Timestamps follow format day/month/year:hour:min:sec+offset",
            "e.g. 05/Oct/2016:10:35:00-0400"
    };

    public static void main(String[] args) {
        if (args.length < 2) {
            // TODO: add help message
            for (String line : help_text) System.out.println(line);
        } else {
            Params params = Params.parse(args);
            Predicate<LogLine> predicate;
            if (params.before != null || params.after != null) {
                predicate = (line) -> line.timestamp != null;
                if (params.before != null) predicate = predicate.and((line) -> line.timestamp.before(params.before));
                if (params.after != null) predicate = predicate.and((line) -> line.timestamp.after(params.after));
            } else {
                predicate = (whatever) -> true;
            }

            Stream<Either<Path, String>> files = TreeWalk.filesFirst(Paths.get(params.dir_name));
            final Pattern final_patern = params.pattern;
            final Predicate<LogLine> final_predicate = predicate;
            Stream<Either<Hit, String>> hits = files.flatMap(file_entry -> file_entry.is_success ?
                    new SearchInFile(file_entry.asSuccess(), final_predicate).forRegexp(final_patern) :
                    Stream.of(fail(file_entry.asFailure())));
            Iterator<Either<Hit, String>> hits_iter = hits.iterator();
            while (hits_iter.hasNext()) {
                Either<Hit, String> result = hits_iter.next();
                // This is crude, admittedly.
                if (result.is_success) {
                    Hit hit = result.asSuccess();
                    System.out.println(hit);
                } else {
                    System.out.println(result.asFailure());
                }
            }
        }
    }
}

class Params {  // Makeshift.
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ssZ");
    private static Pattern CMD_LINE_PATTERN = Pattern.compile(
            "(?:before (\\S+)\\s+)?(?:after (\\S+)\\s+)?(\\S+)?\\s+(\\S+)?");
    Date before = null;
    Date after = null;
    Pattern pattern = null;
    String dir_name = null;

    static Params parse(String[] args) {
        Params params = new Params();
        String cmdline = String.join(" ", args);
        Matcher matcher = CMD_LINE_PATTERN.matcher(cmdline);
        if (! matcher.matches()) die("Cannot parse command lime '" + cmdline + "'");
        params.before = getDateOrDie(matcher.group(1));
        params.after = getDateOrDie(matcher.group(2));
        params.pattern = getPatternOrDie(matcher.group(3));
        params.dir_name = matcher.group(4);
        if (params.dir_name == null) die("Directory name missing");
        return params;
    }

    private static Date getDateOrDie(String date_string) {
        if (date_string == null) return null;
        Date result = DATE_FORMAT.parse(date_string, new ParsePosition(0));
        if (result == null) die("Invalid date " + date_string);
        return result;
    }

    private static Pattern getPatternOrDie(String regexp) {
        if (regexp == null) die("Regexp required");
        try {
            return Pattern.compile(regexp);
        } catch (PatternSyntaxException e) {
            die(e.getMessage());
            return null; // make compiler happy.
        }
    }
    static void die(String message) {
        System.err.println(message);
        System.exit(1);
    }

}