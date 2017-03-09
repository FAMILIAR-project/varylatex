import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.familiar.FMLTest;
import fr.familiar.fm.converter.FeatureModelToExpression;
import fr.familiar.operations.ExpressionUtility;
import fr.familiar.operations.FormulaAnalyzer;
import fr.familiar.operations.featureide.SATFMLFormula;
import fr.familiar.parser.DoubleVariable;
import fr.familiar.variable.*;
import gsd.synthesis.Expression;
import gsd.synthesis.ExpressionType;
import gsd.synthesis.ExpressionUtil;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
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

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.*;
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
    public void testChoco() throws Exception {
        _modelAndSolve();

    }

    public void _modelAndSolve(){
        Model model = new Model("LATEX");

        BoolVar ACK = model.boolVar("ACK");
        BoolVar LONG_AFFILIATION = model.boolVar("LONG_AFFILIATION");
        IntVar stretch = model.intVar("stretch", 970, 1000);
        IntVar vspace = model.intVar("vspace", 0, 5);


        Solver solver = model.getSolver();

        java.util.Random r = new java.util.Random();
        long l = r.nextLong();

       /*
        solver.setSearch(Search.intVarSearch(
                    // selects the variable of smallest domain size
                    new FirstFail(model),
                    // selects the smallest domain value (lower bound)
                    //new IntDomainMin(),
                    new IntDomainRandom(l),
                    // apply equality (var = val)
                    DecisionOperator.int_eq,
                    // variables to branch on
                    stretch, vspace
        ),
        Search.randomSearch(
                    new IntVar[] { ACK, LONG_AFFILIATION }, l)
        );*/

       /*  solver.setSearch( Search.randomSearch(
                    new IntVar[] { ACK, LONG_AFFILIATION, stretch, vspace }, l)
        );*/

        solver.setSearch( Search.randomSearch(
                new IntVar[] { ACK }, l),
                Search.randomSearch(
                        new IntVar[] { LONG_AFFILIATION }, l),
                Search.randomSearch(
                        new IntVar[] { stretch }, l),
                Search.randomSearch(
                        new IntVar[] { vspace }, l));


        int MAX_SOL = 10;
        int nSol = 0;
        while(solver.solve()) {
            // do something, e.g. print out variable values
            //        solver.showStatistics();
            // solver.showSolutions();
            System.out.println("sol=" + solver.findSolution());
            solver.setRestartOnSolutions();
            if (nSol++ > MAX_SOL)
                break;
        }


    }




    @Test
    public void testFM2Choco() throws Exception {
        FeatureModelVariable fmv = FM ("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] ; ACK : [MORE_ACK] [BOLD_ACK]; LONG_AFFILIATION : [EMAIL]  ; ");
        // !EMAIL;
        // fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 5, 10)); // TODO: type the attribute
        //  fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "size_tux", new DoubleDomainVariable("", 3.0, 5.0)); // TODO: type the attribute

        Model model = new FMLChocoModel().transform(fmv);




       Solver solver = model.getSolver();
      // solver.showStatistics();
      // solver.showSolutions();
//        solver.solve();
        // solver.getA

        int MAX_SOL = 100;
        int nSol = 0;

        while(solver.solve()) {
            // do something, e.g. print out variable values
            //solver.showStatistics();
            //solver.showSolutions();
            Solution sol = solver.findSolution();
            //_log.info("sol" + nSol + "=" + sol);
            // solver.setRestartOnSolutions();
            if (nSol++ > MAX_SOL)
                break;
        }

        assertEquals(fmv.counting(), (double) solver.getSolutionCount(), 0.0);




    }





    @Test
    public void test2() throws Exception {

        Logger.getLogger("ConfigurationToMap").setLevel(Level.WARNING);
       // Logger.getGlobal().setLevel(Level.OFF);

       // _log.setLevel(Level.OFF);

        // basic parameter: the LaTeX main file
        String latexFileName = "mySubmission";

        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, "input/", "tex"))
                .build();
        Mustache mustache = engine.getMustache(latexFileName);


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
           // Map<String, Object> orderedConf = new ConfigurationToMap(fmv).populateAttributeValuesAndConfs2map(cf);

            int NB_REPEAT = 10;
            Collection<Map<String, Object>> orderedConfs = new HashSet<Map<String, Object>>();
            for (int i = 0; i < NB_REPEAT; i++)
                orderedConfs.add(new ConfigurationToMap(fmv).populateAttributeValuesAndConfs2map(cf)); // very naive since they could be duplicated "confs" (eg the case in which the attribute is only an integer value)

            for (Map<String, Object> orderedConf : orderedConfs) {
                idConf++;
                JsonObject jSonConf = new ConfigurationToJSon(fmv).confs2JSON(orderedConf);

                renderConfiguration(jSonConf, mustache, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".tex");

                serializeConfigurationJSON(jSonConf, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".json");
                serializeConfigurationCSV(orderedConf, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".csv");

            }


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


    @Test
    public void testFSE() throws Exception {

        Logger.getLogger("ConfigurationToMap").setLevel(Level.WARNING);

        String FSE_TARGET_FOLDER = "output-FSE";

        // basic parameter: the LaTeX main file
        String latexFileName = "VaryingVariability-FSE15";

        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, "input/FSE-newidea/", "tex"))
                .build();
        Mustache mustache = engine.getMustache(latexFileName);

        FeatureModelVariable fmv = FM ("VARY_LATEX : BIB [ACK] [LONG_AFFILIATION] ; ");
        fmv.setFeatureAttribute(fmv.getFeature("BIB"), "vspace_bib", new DoubleDomainVariable("", 0.0, 5.0)); // TODO: type the attribute
        //fmv.setFeatureAttribute(fmv.getFeature("BIB"), "stretch", new DoubleDomainVariable("", 0.98, 1.0)); // TODO: type the attribute
        fmv.setFeatureAttribute(fmv.getFeature("BIB"), "stretch", new DoubleVariable("", 0.99)); // TODO: type the attribute


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
            // Map<String, Object> orderedConf = new ConfigurationToMap(fmv).populateAttributeValuesAndConfs2map(cf);


            // TODO: manage the case in which there is only one attributes and its value is fixed
            int NB_REPEAT = 40;
            Collection<Map<String, Object>> orderedConfs = new HashSet<Map<String, Object>>();
            for (int i = 0; i < NB_REPEAT; i++)
                orderedConfs.add(new ConfigurationToMap(fmv).populateAttributeValuesAndConfs2map(cf)); // very naive since they could be duplicated "confs" (eg the case in which the attribute is only an integer value)

            for (Map<String, Object> orderedConf : orderedConfs) {
                idConf++;
                JsonObject jSonConf = new ConfigurationToJSon(fmv).confs2JSON(orderedConf);

                renderConfiguration(jSonConf, mustache, FSE_TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".tex");

                serializeConfigurationJSON(jSonConf, FSE_TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".json");
                serializeConfigurationCSV(orderedConf, FSE_TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".csv");

            }


        }


        FileWriter fw = new FileWriter(new File(FSE_TARGET_FOLDER + "/" + "headerftscsv" + ".txt"));
        Set<String> fts = fmv.features().names();
        // also attributes!
        Collection<FeatureAttribute> allAttrs = _collectAllAttributes(fmv);
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
