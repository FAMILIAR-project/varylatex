import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xalan.internal.utils.FeatureManager;
import fr.familiar.FMLTest;
import fr.familiar.parser.DoubleVariable;
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


    @Test
    public void testFMDemo() throws Exception {

        // INPUT
        FeatureModelVariable fmv = FM ("VARY_LATEX : [VSPACE_FIGURE_TUX] [ACK] [LONG_AFFILIATION]; ACK : [MORE_ACK] [BOLD_ACK]; ");
        fmv.setFeatureAttribute(fmv.getFeature("VSPACE_FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 0, 4)); // TODO: type the attribute
        _log.info(fmv.getFeature("VSPACE_FIGURE_TUX").lookup("vspace_tux").getValue());

        Collection<FeatureAttribute> allAttrs = _collectAllAttributes(fmv);
        _log.info("attributes: " + allAttrs);

        // map of configuration values
        Map<String, Object> confs = new HashMap<>();

        Map<String, List<FeatureAttribute>> attrs = fmv.getFeatureAttributes();
        Set<String> fts = attrs.keySet();
        for (String ft: fts) {
            List<FeatureAttribute> atts = attrs.get(ft);
            for (FeatureAttribute att : atts ) {
                _log.info(ft + "[@" + att.getName() + "] = " + att.getValue());

                Variable domain = att.getValue();
                if (domain instanceof IntegerDomainVariable) {
                   IntegerDomainVariable dom = (IntegerDomainVariable) domain;
                    int min = dom.getMin();
                    int max = dom.getMax();

                    int confValue = (int) (Math.random() * (max - min)) + min;
                    _log.info("" + confValue);
                    confs.put(att.getName(), new Integer(confValue));

                }

                if (domain instanceof IntegerVariable) {

                }

                if (domain instanceof DoubleVariable) {

                }


            }

        }

        confs.entrySet().forEach(conf -> _log.info(conf.getKey() + " => " + conf.getValue()));


    }

    private Collection<FeatureAttribute> _collectAllAttributes(FeatureModelVariable fmv) {

        Map<String,List<FeatureAttribute>> cAttrs = fmv.getFeatureAttributes();
        Set<String> ftWithAttrs
                = cAttrs.keySet();

        Collection<FeatureAttribute> allAttrs = new HashSet<>();
        for (String ft: ftWithAttrs) {
            allAttrs.addAll(cAttrs.get(ft));
        }
        return allAttrs;
    }

    @Test
    public void test2() throws Exception {

        // basic parameter: the LaTeX main file
        String latexFileName = "mySubmission";

        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, "input/", "tex"))
                .build();
        Mustache mustache = engine.getMustache(latexFileName);



        FeatureModelVariable fmv = FM ("VARY_LATEX : [SUBTITLE] [VSPACE_FIGURE_TUX] [ACK] [LONG_AFFILIATION]; ACK : [MORE_ACK] [BOLD_ACK]; ");
        fmv.setFeatureAttribute(fmv.getFeature("VSPACE_FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 5, 10)); // TODO: type the attribute
        // fmv.setFeatureAttribute(fmv.getFeature("SIZE_FIGURE_TUX"), "size_tux", new DoubleDomainVariable("", 3.2, 3.3)); // TODO: type the attribute

        _log.info(fmv.getFeature("VSPACE_FIGURE_TUX").lookup("vspace_tux").getValue());
        //_log.info(fmv.getFeature("SIZE_FIGURE_TUX").lookup("size_tux").getValue());


        Set<Variable> cfs = fmv.configs();
        Collection<Set<String>> scfs = new HashSet<Set<String>>();
        for (Variable cf : cfs) {
            scfs.add(((SetVariable) cf).names());
        }


        int idConf = 0;
        for (Set<String> cf : scfs) {
            idConf++;

            //assertEquals(5, conf.entrySet().size());
            // _log.info("conf: " + conf);
            // render configuration in the template


           // if (idConf > 3)
             //   break ;
            Map<String, Object> orderedConf = new ConfigurationToMap(fmv).confs2map(cf);
            JsonObject jSonConf = new ConfigurationToJSon(fmv).confs2JSON(orderedConf);

            renderConfiguration(jSonConf, mustache, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".tex");

            serializeConfigurationJSON(jSonConf, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".json");
            serializeConfigurationCSV(orderedConf, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".csv");
        }


        FileWriter fw = new FileWriter(new File(TARGET_FOLDER + "/" + "headerftscsv" + ".txt"));
        Set<String> fts = fmv.features().names();
        // also attributes!
        Collection<FeatureAttribute> allAttrs = _collectAllAttributes(fmv);
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




    private void serializeConfigurationJSON(JsonObject conf, String outputFileLocation) throws Exception {
        Gson g = new Gson();
        FileWriter fw = new FileWriter(new File(outputFileLocation));
        fw.write(g.toJson(conf));
        fw.close();

    }

    private void serializeConfigurationCSV(Map<String, Object> conf, String outputFileLocation) throws Exception {

        FileWriter fw = new FileWriter(new File(outputFileLocation));

        // String str = conf.keySet().stream().sorted().map(ft -> (conf.get(ft) ? "true" : "false")).collect(Collectors.joining(","));

        String str = conf.keySet().stream().sorted().map(ft -> ConfigurationToJSon._convertConfigurationObjectValue(conf.get(ft)).toString()).collect(Collectors.joining(","));
        fw.write(str + "\n");
        fw.close();

    }




    private void renderConfiguration(JsonObject conf, Mustache mustache, String outputLatexFileLocation) throws Exception {

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
