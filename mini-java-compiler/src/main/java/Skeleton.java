import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Skeleton {

    public static void main (String[] args) {
        ArrayList arguments = new ArrayList(Arrays.asList(args));

        if (arguments.contains(new String("--echo"))) {
            final int index              = arguments.indexOf(new String("--echo"));
            final String fileName        = arguments.get(index + 1).toString();
            final FileInputStream in     = null;
            final Charset ENCODING       = StandardCharsets.UTF_8;
            final Path path              = Paths.get(fileName);

            try {
                final List<String> lines = Files.readAllLines(path, ENCODING);

                lines.forEach(line -> System.out.println(line));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Please run with \"--echo <file-name>\".");
        }

    }
}
