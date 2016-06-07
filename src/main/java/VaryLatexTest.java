import org.junit.Test;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.FileSystemTemplateLocator;
import org.trimou.util.ImmutableMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by macher1 on 06/06/2016.
 */
public class VaryLatexTest {


    private static String TARGET_FOLDER = "output";

    @Test
    public void test1() throws IOException {


        // basic parameter: the LaTeX main file
        String latexFileName = "mySubmission";

        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, "input/", "tex"))
                .build();


        Mustache mustache = engine.getMustache(latexFileName);

        //List<Configuration> valueTemplates = new ArrayList<>();

        // It's possible to pass a java.lang.Appendable impl, e.g. any java.io.Writer
        String output = mustache.render(ImmutableMap.<String, Object> of(
                        "ACK", true,
                        "BOLD_ACK", false,
                        "MORE_ACK", true)
        );
        //String output = mustache.render(new Configuration(false, "Guillaume B. and Quentin P.")); // valueTemplates);

        FileWriter fw = new FileWriter(new File(TARGET_FOLDER + "/" + latexFileName + ".tex"));
        fw.write(output);
        fw.close();


    }


    /*
    public class Configuration {

        private boolean ack;

        private String friends;

        public Configuration(boolean ack, String friends) {
            this.ack = ack;
            this.friends = friends;
        }

        public boolean isAck() {
            return ack;
        }

        public String getFriends() {
            return friends;
        }

    }*/


}
