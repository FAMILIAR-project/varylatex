import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.familiar.FMLTest;
import fr.familiar.experimental.afm.*;
import fr.familiar.variable.*;
import org.junit.Test;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.FileSystemTemplateLocator;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by macher1 on 06/06/2016.
 */
public class VaryLatexTest extends FMLTest {

    private static Logger _log = Logger.getLogger("VaryLatexTest");

    @Test
    public void testGenVariantsWithChoco() throws Exception {

        /**
         * TEMPLATE SETTING
         */
        // basic parameter: the LaTeX main file
        String latexFileName = "mySubmission";
        String inputFolder = "input/"; // LaTeX source files
        String TARGET_FOLDER = "output";

        /**
         * FEATURE MODEL
         */

        FeatureModelVariable fmv = FM ("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] ; ACK : [MORE_ACK] [BOLD_ACK]; LONG_AFFILIATION : [EMAIL]  ; ");
        fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 5, 10));
        fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "size_tux", new DoubleDomainVariable("", 3.0, 5.0, 10.0));


        /***
         * CONFIG GEN (with Choco model/solver)
         */
        // we derive 100 configurations here
        int NB_REPEAT = 100;
        Collection<FMLChocoConfiguration> scfs = new FMLChocoSolver(fmv).configs(NB_REPEAT);

        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, inputFolder, "tex"))
                .build();
        Mustache mustache = engine.getMustache(latexFileName);


        /*
         * Store each config into a CSV and resolve variability within templates based on a config
         */

        int idConf = 0;
        for (FMLChocoConfiguration cf : scfs) {
            idConf++;

            JsonObject jSonConf = new ConfigurationToJSon(fmv).confs2JSON(cf.getValues());

            deriveLaTeXFileFromConfiguration(jSonConf, mustache, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".tex");
            writeCSVLineOfAConfiguration(cf.getValues(), TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".csv"); // TODO WEIRD (basically we *assume* a sorted collection for CSV headers and cell values)
        }


        /*
         * Serialize the whole CSV
         */

        FileWriter fw = new FileWriter(new File(TARGET_FOLDER + "/" + "headerftscsv" + ".txt"));
        Set<String> fts = fmv.features().names();
        // also attributes!
        Collection<FeatureAttribute> allAttrs = AFMUtils.collectAllAttributes(fmv);
        allAttrs.stream().forEach(e -> fts.add(e.getName()));

        fw.write(fts.stream().sorted().collect(Collectors.joining(","))); // TODO WEIRD (basically we *assume* a sorted collection for CSV headers and cell values)
        fw.close();


        /*
         * Derive the final product and observe (nbPages, size)
         */
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


    /*
     * A demonstration of VaryLaTeX with a real-world example (FSE paper)
     */

    @Test
    public void testFSE() throws Exception {

        /**
         * TEMPLATE SETTING
         */
        String inputFolder = "input/FSE-newidea/"; // LaTeX source files
        String latexFileName = "VaryingVariability-FSE15"; // LaTeX main file
        String FSE_TARGET_FOLDER = "output-FSE"; // targeted folder of all PDFs and stats

        /*
         *  variability model (attributed feature model)
         */

        FeatureModelVariable fmv = FM ("VARY_LATEX : BREF BIB [PL_FOOTNOTE] [ACK] JS_STYLE [LONG_AFFILIATION] ; " +
                "LONG_AFFILIATION : [EMAIL]; JS_STYLE : (JS_SCRIPTSIZE|JS_TINY|JS_FOOTNOTESIZE); " +
                "ACK : [LONG_ACK] (BOLD_ACK|PARAGRAPH_ACK); !JS_FOOTNOTESIZE; ");
                    //  footnotesize is obviously too large (we learned that)
        fmv.setFeatureAttribute(fmv.getFeature("BIB"), "vspace_bib", new DoubleDomainVariable("", 1.0, 5.0, 10.0));
        fmv.setFeatureAttribute(fmv.getFeature("BREF"), "bref_size", new DoubleDomainVariable("", 0.7, 1.0, 10.0));
        fmv.setFeatureAttribute(fmv.getFeature("BREF"), "cserver_size", new DoubleDomainVariable("", 0.6, 0.9, 10.0));

        /***
         * The rest is automatic!
         * CONFIG GEN (with Choco model/solver)
         */
        Collection<FMLChocoConfiguration> scfs = new FMLChocoSolver(fmv).configs(10);

        // Using Mustache to resolve variability within LaTeX files
        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, inputFolder, "tex"))
                .build();
        Mustache mustache = engine.getMustache(latexFileName);

        /*
         * Store each config into a CSV and resolve variability within templates based on a config
         */

        int idConf = 0;
        for (FMLChocoConfiguration cf : scfs) {
            idConf++;

            JsonObject jSonConf = new ConfigurationToJSon(fmv).confs2JSON(cf.getValues());

            deriveLaTeXFileFromConfiguration(jSonConf, mustache, FSE_TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".tex");
            writeCSVLineOfAConfiguration(cf.getValues(), FSE_TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".csv"); // TODO WEIRD (basically we *assume* a sorted collection for CSV headers and cell values)
        }



        /*
         * Serialize the whole CSV
         */


        FileWriter fw = new FileWriter(new File(FSE_TARGET_FOLDER + "/" + "headerftscsv" + ".txt"));
        Set<String> fts = fmv.features().names();
        // also attributes!
        Collection<FeatureAttribute> allAttrs = AFMUtils.collectAllAttributes(fmv);
        allAttrs.stream().forEach(e -> fts.add(e.getName()));

        fw.write(fts.stream().sorted().collect(Collectors.joining(",")));
        fw.close();




        ProcessBuilder pb = new ProcessBuilder("./allCompile.sh");
        pb.directory(new File("" + FSE_TARGET_FOLDER));

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

    /*
    public static void serializeConfigurationJSON(JsonObject conf, String outputFileLocation) throws Exception {
        Gson g = new Gson();
        FileWriter fw = new FileWriter(new File(outputFileLocation));
        fw.write(g.toJson(conf));
        fw.close();

    }*/



    /*
   * ad-hoc helper to write a CSV line of a configuration (values of options)
    */
    public static void writeCSVLineOfAConfiguration(Map<String, Object> conf, String outputFileLocation) throws Exception {
        FileWriter fw = new FileWriter(new File(outputFileLocation));
        String str = conf.keySet().stream().sorted().map(ft -> ConfigurationToJSon._convertConfigurationObjectValue(conf.get(ft)).toString()).collect(Collectors.joining(","));
        fw.write(str + "\n");
        fw.close();
    }

    /*
    * ad-hoc helper to derive a latex file variant based on a configuration using Mustache
    * output is a new texfile
     */
    public static void deriveLaTeXFileFromConfiguration(JsonObject conf, Mustache mustache, String outputLatexFileLocation) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> pr : conf.entrySet()) {
            JsonElement vl = pr.getValue();
            _log.info("" + pr.getKey() + " => " + vl);
            assertTrue(vl.isJsonPrimitive()); // assumption


            String vlStr = vl.getAsString();
            //map.put(pr.getKey(), b);
           if ("true".equals(vlStr.toString()) || "false".equals(vlStr.toString())) {
                _log.info("TRUE VALUE");
                map.put(pr.getKey(), Boolean.parseBoolean(vlStr.toString()));
            }
            else
              map.put(pr.getKey(), vl.toString());
        }


        _log.info("MAP" + map);
        String output = mustache.render(map);
        //output = output.replace("\"true\"", "true");
        //output = output.replace("\"false\"", "false");

        FileWriter fw = new FileWriter(new File(outputLatexFileLocation));
        fw.write(output);
        fw.close();

    }












}
