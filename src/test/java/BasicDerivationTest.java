import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.familiar.FMLTest;
import fr.familiar.experimental.afm.ConfigurationToJSon;
import fr.familiar.experimental.afm.ConfigurationToMap;
import fr.familiar.experimental.afm.DoubleDomainVariable;
import fr.familiar.experimental.afm.IntegerDomainVariable;
import fr.familiar.variable.FeatureAttribute;
import fr.familiar.variable.FeatureModelVariable;
import fr.familiar.variable.SetVariable;
import fr.familiar.variable.Variable;
import org.junit.Test;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.FileSystemTemplateLocator;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * very basic/naive attempts
 * Created by macher1 on 08/02/2018.
 */
@Deprecated
public class BasicDerivationTest extends FMLTest {


    private static Logger _log = Logger.getLogger("BasicDerivationTest ");


    /*
   * giving a configuration as input (with a map key/value)
   * provide a LaTeX file with resolved variability
   * very basic illustration
   *
   */
    @Test
    public void testOneConfiguration() throws IOException {


        String TARGET_FOLDER = "output";

        // basic parameter: the LaTeX main file
        String latexFileName = "mySubmission";

        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, "input/", "tex"))
                .build();


        Mustache mustache = engine.getMustache(latexFileName);

        //List<Configuration> valueTemplates = new ArrayList<>();

        // It's possible to pass a java.lang.Appendable impl, e.g. any java.io.Writer

        Gson gson = new Gson();

        JsonObject conf = new JsonParser().parse("{\n" +
                "  \"LONG_AFFILIATION\": true,\n" +
                "  \"ACK\": true,\n" +
                "  \"MORE_ACK\": false,\n" +
                "  \"BOLD_ACK\": false\n" +
                "}").getAsJsonObject();

        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> pr : conf.entrySet()) {
            JsonElement vl = pr.getValue();
            assertTrue (vl.isJsonPrimitive()); // assumption
            map.put(pr.getKey(), Boolean.parseBoolean(vl.toString()));
        }

        assertEquals(4, map.size());

        System.err.println("map : " + map);

        String output = mustache.render(map);

        /*
        String output = mustache.render(ImmutableMap.<String, Object> of(
                        "ACK", true,
                        "BOLD_ACK", false,
                        "MORE_ACK", true)
        );*/
        //String output = mustache.render(new Configuration(false, "Guillaume B. and Quentin P.")); // valueTemplates);

        FileWriter fw = new FileWriter(new File(TARGET_FOLDER + "/" + latexFileName + ".tex"));
        fw.write(output);
        fw.close();


    }





    /*
    * generate several configurations from a feature model
    * and for each configuration we derive a paper variant
    * basic example with 1-page LaTeX
    * deprecated since the config gen is based on a old and naive implementation (without Choco)
    */
    @Deprecated
    @Test
    public void testGenerateVariants() throws Exception {

        Logger.getLogger("fr.familiar.experimental.afm.ConfigurationToMap").setLevel(Level.WARNING);

        String TARGET_FOLDER = "output";
        // basic parameter: the LaTeX main file
        String latexFileName = "mySubmission";

        // see folder "input"
        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, "input/", "tex"))
                .build();
        Mustache mustache = engine.getMustache(latexFileName);

        // we derive 10 configurations here
        int NB_REPEAT = 10;


//        FeatureModelVariable fmv = FM ("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] ; ACK : [MORE_ACK] [BOLD_ACK]; ");
// FeatureModelVariable fmv = FM ("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] [EMAIL] ; ACK : [MORE_ACK] [BOLD_ACK]; EMAIL -> LONG_AFFILIATION ; ");
        FeatureModelVariable fmv = FM ("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] ; ACK : [MORE_ACK] [BOLD_ACK]; LONG_AFFILIATION : [EMAIL]  ; ");
        fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 5, 10)); // TODO: type the attribute
        fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "size_tux", new DoubleDomainVariable("", 3.0, 5.0)); // TODO: type the attribute

        _log.info(fmv.getFeature("FIGURE_TUX").lookup("vspace_tux").getValue());
        _log.info(fmv.getFeature("FIGURE_TUX").lookup("size_tux").getValue());



        Set<Variable> cfs = fmv.configs();
        Collection<Set<String>> scfs = new HashSet<Set<String>>();
        for (Variable cf : cfs) {
            Set<String> confFts = ((SetVariable) cf).names();
            scfs.add(confFts);
            //_log.info("confFts:" + confFts);
        }
        /*
        if (true)
            return;*/

        assertEquals(scfs.size(), cfs.size());


        int idConf = 0;
        for (Set<String> cf : scfs) {
            // idConf++;

            //assertEquals(5, conf.entrySet().size());
            // _log.info("conf: " + conf);
            // render configuration in the template


            // if (idConf > 3)
            //   break ;
            // Map<String, Object> orderedConf = new fr.familiar.experimental.afm.ConfigurationToMap(fmv).populateAttributeValuesAndConfs2map(cf);


            Collection<Map<String, Object>> orderedConfs = new HashSet<Map<String, Object>>();
            for (int i = 0; i < NB_REPEAT; i++)
                orderedConfs.add(new ConfigurationToMap(fmv).populateAttributeValuesAndConfs2map(cf)); // very naive since they could be duplicated "confs" (eg the case in which the attribute is only an integer value)

            for (Map<String, Object> orderedConf : orderedConfs) {
                idConf++;
                JsonObject jSonConf = new ConfigurationToJSon(fmv).confs2JSON(orderedConf);
                VaryLatexTest.deriveLaTeXFileFromConfiguration(jSonConf, mustache, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".tex");
                VaryLatexTest.writeCSVLineOfAConfiguration(orderedConf, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".csv");
            }

        }


        FileWriter fw = new FileWriter(new File(TARGET_FOLDER + "/" + "headerftscsv" + ".txt"));
        Set<String> fts = fmv.features().names();
        // also attributes!
        Collection<FeatureAttribute> allAttrs = AFMUtils.collectAllAttributes(fmv);
        allAttrs.stream().forEach(e -> fts.add(e.getName()));

        fw.write(fts.stream().sorted().collect(Collectors.joining(",")));
        fw.close();




        ProcessBuilder pb = new ProcessBuilder("./allCompile.sh");
        pb.directory(new File("/Users/macher1/Documents/SANDBOX/varylatex/output/"));

        Process pr = pb.start();

        //Read output
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = null, previous = null;
        while ((line = br.readLine()) != null)
            if (!line.equals(previous)) {
                previous = line;
                out.append(line).append('\n');
                _log.info(line);
            }

        //Check result
        if (pr.waitFor() == 0)
            _log.info("Success!");
        pr.destroy();

    }
}
