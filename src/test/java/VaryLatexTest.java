import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.familiar.FMLTest;
import fr.familiar.parser.DoubleVariable;
import fr.familiar.variable.*;
import fr.familiar.variable.Variable;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.DecisionTreeClassificationModel;
import org.apache.spark.ml.classification.DecisionTreeClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.*;
import org.apache.spark.mllib.tree.DecisionTree;
import org.apache.spark.mllib.tree.configuration.Algo;
import org.apache.spark.mllib.tree.configuration.Strategy;
import org.apache.spark.mllib.tree.impurity.Gini;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.*;
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
 * Created by macher1 on 06/06/2016.
 */
public class VaryLatexTest extends FMLTest {

    private static Logger _log = Logger.getLogger("VaryLatexTest");




    @Test
    public void test1() throws IOException {


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


    // a basic example
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




    // a basic example

    @Test
    public void testFM2Choco() throws Exception {

        FeatureModelVariable[] fmvs = new FeatureModelVariable[] {
                FM("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] ; ACK : [MORE_ACK] [BOLD_ACK]; LONG_AFFILIATION : [EMAIL]  ; "),
                FM("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] ; ACK : [MORE_ACK] [BOLD_ACK]; LONG_AFFILIATION : [EMAIL]  ; !EMAIL; "),
                FM("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] ; ACK : [MORE_ACK] [BOLD_ACK]; LONG_AFFILIATION : [EMAIL]  ; MORE_ACK -> !BOLD_ACK; "),
                FM("VARY_LATEX : [SUBTITLE] FIGURE_TUX (ACK|LONG_AFFILIATION); ACK : [MORE_ACK] [BOLD_ACK]; LONG_AFFILIATION : [EMAIL]  ; MORE_ACK -> !BOLD_ACK; "),
                FM("VARY_LATEX : [SUBTITLE] FIGURE_TUX ACK LONG_AFFILIATION; ACK : (MOREACK|BOLDACK); LONG_AFFILIATION : [EMAIL]  ; "),
                FM("VARY_LATEX : (MOREACK|BOLDACK)+; "),
                FM("VARY_LATEX : (MOREACK|BOLDACK)?; "),
                FM("VARY_LATEX : (MORE_ACK|BOLD_ACK); ")


        };

        // !EMAIL;
        //
        for (FeatureModelVariable fmv : fmvs) {
            //fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 5, 10)); // TODO: type the attribute
            //fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "size_tux", new DoubleDomainVariable("", 3.0, 5.0, 10.0)); // TODO: type the attribute


            FMLChocoSolver fmlChocoSolver = new FMLChocoSolver(fmv);
            Collection<FMLChocoConfiguration> cfgs = fmlChocoSolver.configsALL();
            // Collection<FMLChocoConfiguration> cfgs = new FMLChocoSolver(model, fmv).configs((int) fmv.counting());
            for (FMLChocoConfiguration cfg : cfgs) {
                if (cfg == null) // TODO weird
                    break;
                System.out.println("sol=" + cfg.getValues());
            }

// TODO FIXME: I don't like this state based solution (side-effect)
            Solver solver = fmlChocoSolver.getCurrentSolver();
            // solver.showStatistics();
            // solver.showSolutions();

            /*
            int MAX_SOL = 1000;
            int nSol = 0;

            while (solver.solve()) {
                Solution sol = solver.findSolution();
                if (sol == null) // TODO weird
                    break;
               // System.out.println("sol= " + sol);
               System.out.println("sol (map)=" + _mkMapOfSolution(sol, model));
                // solver.setRestartOnSolutions();
                if (nSol++ > MAX_SOL)
                    break;
            }*/


            // side-effect
            // works because getSolutionCount() returns the number of solving you have made (basically number of calls to "solve" / findSolution)
            assertEquals(fmv.counting(), (double) solver.getSolutionCount(), 0.0);
            assertEquals(fmv.counting(), cfgs.size(), 0.0);
        }

    }

    @Test
    public void testFM2ChocoAttributes() throws Exception {



        FeatureModelVariable fmv = FM("VARY_LATEX : [SUBTITLE] FIGURE_TUX ACK LONG_AFFILIATION; ACK : (MOREACK|BOLDACK); LONG_AFFILIATION : [EMAIL]  ; ");
        fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 5, 10)); // TODO: type the attribute
        fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "size_tux", new DoubleDomainVariable("", 3.0, 5.0, 100.0)); // TODO: type the attribute


        Collection<AttributedConstraintVariable> cstsAtts = new HashSet<>();
        cstsAtts.add(new AttributedConstraintVariable(new AttributedExpression("vspace_tux", ArithmeticCompOperator.GE, 7)));
        cstsAtts.add(new AttributedConstraintVariable(new AttributedExpression("size_tux", ArithmeticCompOperator.GE, 4.9)));

        FMLChocoSolver fmlChocoSolver = new FMLChocoSolver(fmv, cstsAtts);
        Collection<FMLChocoConfiguration> cfgs = fmlChocoSolver.configsALL();
        //Collection<FMLChocoConfiguration> cfgs = fmlChocoSolver.configs(3);
        int c = 0;
        for (FMLChocoConfiguration cfg : cfgs) {
            _log.info("cfg (" + c++ + ") = " + cfg.getValues());
        }

        /*
        Solver solver = fmlChocoSolver.getCurrentSolver();
        // side-effect
        // works because getSolutionCount() returns the number of solving you have made (basically number of calls to "solve" / findSolution)
        double solutionCount = (double) solver.getSolutionCount();
        _log.warning("solutionCount " + solutionCount);
        assertEquals(fmv.counting(), solutionCount, 0.0);
        assertEquals(fmv.counting(), cfgs.size(), 0.0);*/


    }





    @Test
    public void test2() throws Exception {

        String TARGET_FOLDER = "output";

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


        Model model = new FMLChocoModel().transform(fmv);


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
    public void test2WithChoco() throws Exception {


        String TARGET_FOLDER = "output";

        /**
         * TEMPLATE SETTING
         */
        // basic parameter: the LaTeX main file
        String latexFileName = "mySubmission";

        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, "input/", "tex"))
                .build();
        Mustache mustache = engine.getMustache(latexFileName);


        /**
         * FEATURE MODEL
         */

        FeatureModelVariable fmv = FM ("VARY_LATEX : [SUBTITLE] FIGURE_TUX [ACK] [LONG_AFFILIATION] ; ACK : [MORE_ACK] [BOLD_ACK]; LONG_AFFILIATION : [EMAIL]  ; ");
        fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 5, 10));
        fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "size_tux", new DoubleDomainVariable("", 3.0, 5.0, 10.0));


        /***
         * CONFIG GEN (with Choco model/solver)
         */


        Collection<FMLChocoConfiguration> scfs = new FMLChocoSolver(fmv).configs(300);


        /*
         * Store each config into a CSV and resolve variability within templates based on a config
         */

        int idConf = 0;
        for (FMLChocoConfiguration cf : scfs) {
            idConf++;

            JsonObject jSonConf = new ConfigurationToJSon(fmv).confs2JSON(cf.getValues());

            renderConfiguration(jSonConf, mustache, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".tex");

            serializeConfigurationJSON(jSonConf, TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".json");
            serializeConfigurationCSV(cf.getValues(), TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".csv"); // TODO WEIRD (basically we *assume* a sorted collection for CSV headers and cell values)
        }


        /*
         * Serialize the whole CSV
         */

        FileWriter fw = new FileWriter(new File(TARGET_FOLDER + "/" + "headerftscsv" + ".txt"));
        Set<String> fts = fmv.features().names();
        // also attributes!
        Collection<FeatureAttribute> allAttrs = _collectAllAttributes(fmv);
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




    @Test
    public void testFSE() throws Exception {

        /**
         * TEMPLATE SETTING
         */

        String FSE_TARGET_FOLDER = "output-FSE";

        // basic parameter: the LaTeX main file
        String latexFileName = "VaryingVariability-FSE15";

        MustacheEngine engine = MustacheEngineBuilder
                .newBuilder()
                .addTemplateLocator(new FileSystemTemplateLocator(1, "input/FSE-newidea/", "tex"))
                .build();
        Mustache mustache = engine.getMustache(latexFileName);



        FeatureModelVariable fmv = FM ("VARY_LATEX : BIB [ACK] [LONG_AFFILIATION] ; ");
        fmv.setFeatureAttribute(fmv.getFeature("BIB"), "vspace_bib", new DoubleDomainVariable("", 0.0, 5.0, 10.0)); // TODO: type the attribute
        fmv.setFeatureAttribute(fmv.getFeature("BIB"), "stretch", new DoubleDomainVariable("", 0.98, 1.0, 1000.0)); // TODO: type the attribute
        // fmv.setFeatureAttribute(fmv.getFeature("BIB"), "stretch", new DoubleVariable("", 0.99)); // TODO: type the attribute

        /***
         * CONFIG GEN (with Choco model/solver)
         */


        Collection<FMLChocoConfiguration> scfs = new FMLChocoSolver(fmv).configs(300);


        /*
         * Store each config into a CSV and resolve variability within templates based on a config
         */

        int idConf = 0;
        for (FMLChocoConfiguration cf : scfs) {
            idConf++;

            JsonObject jSonConf = new ConfigurationToJSon(fmv).confs2JSON(cf.getValues());

            renderConfiguration(jSonConf, mustache, FSE_TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".tex");

            serializeConfigurationJSON(jSonConf, FSE_TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".json");
            serializeConfigurationCSV(cf.getValues(), FSE_TARGET_FOLDER + "/" + latexFileName + "_" + idConf + ".csv"); // TODO WEIRD (basically we *assume* a sorted collection for CSV headers and cell values)
        }



        /*
         * Serialize the whole CSV
         */


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

    @Test
    public void testMLib() throws Exception {

        SparkSession spark = SparkSession
                .builder()
                .appName("Java Spark SQL basic example")
                .config("spark.master", "local")
                .getOrCreate();



        // bof
        StructType customSchema = new StructType(new StructField[] {
                new StructField("ACK", DataTypes.BooleanType, true, Metadata.empty()),
                new StructField("BOLD_ACK", DataTypes.BooleanType, true, Metadata.empty()),
                new StructField("EMAIL", DataTypes.BooleanType, true, Metadata.empty()),
                new StructField("size_tux", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("vspace_tux", DataTypes.IntegerType, true, Metadata.empty())
        });

        // Load the data stored in LIBSVM format as a DataFrame.
        Dataset<Row> data = spark
                .read()
                .option("header", "true")
                .format("csv")
          //       .schema(customSchema)
           //     .option("inferSchema", "true")
                .load("output/stats.csv");


        data.show();







        // Index labels, adding metadata to the label column.
        // Fit on whole dataset to include all labels in index.
        StringIndexerModel labelIndexer = new StringIndexer()
                .setInputCol("EMAIL")
                .setOutputCol("indexedEMAIL")
                .fit(data);

        // Index labels, adding metadata to the label column.
        // Fit on whole dataset to include all labels in index.
        StringIndexerModel labelIndexer1 = new StringIndexer()
                .setInputCol("ACK")
                .setOutputCol("indexedACK")
                .fit(data);

        StringIndexerModel labelIndexer2 = new StringIndexer()
                .setInputCol("vspace_tux")
                .setOutputCol("indexedVSPACE_TUX")
                .fit(data);

        StringIndexerModel labelIndexer3 = new StringIndexer()
                .setInputCol("size_tux")
                .setOutputCol("indexedSIZE_TUX")
                .fit(data);


        StringIndexerModel labelIndexerLabel = new StringIndexer()
                .setInputCol("nbPages")
                .setOutputCol("indexedLabel")
                .fit(data);

     //   labelIndexer.transform(data).show();

        // data.show();

        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"indexedEMAIL", "indexedACK", "indexedSIZE_TUX", "indexedVSPACE_TUX"})
                .setOutputCol("indexedFeatures");

        //Dataset<Row> output = assembler.transform(data);
//        System.err.println("OUT: " + output);

        // Automatically identify categorical features, and index them.
        /*
        VectorIndexerModel featureIndexer = new VectorIndexer()
                .setInputCol("vspace_tux")
                .setOutputCol("indexedFeatures")
                .setMaxCategories(4) // features with > 4 distinct values are treated as continuous.
                .fit(data);*/

        // Split the data into training and test sets (30% held out for testing).
        Dataset<Row>[] splits = data.randomSplit(new double[]{0.9, 0.1});
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];

        // Train a DecisionTree model.
        DecisionTreeClassifier dt = new DecisionTreeClassifier()
                .setLabelCol("indexedLabel")
                .setFeaturesCol("indexedFeatures");

        // Convert indexed labels back to original labels.

        IndexToString labelConverter = new IndexToString()
                .setInputCol("prediction")
                .setOutputCol("predictedLabel")
                .setLabels(labelIndexer2.labels());

        Pipeline pipeline = new Pipeline()
                .setStages(new PipelineStage[]{labelIndexer, labelIndexer1, labelIndexer2, labelIndexer3, labelIndexerLabel, assembler, dt, labelConverter});

        // Chain indexers and tree in a Pipeline.
        /*Pipeline pipeline = new Pipeline()
                .setStages(new PipelineStage[]{labelIndexer, featureIndexer, dt, labelConverter});*/

        // Train model. This also runs the indexers.
        PipelineModel model = pipeline.fit(trainingData);

        // Make predictions.
        Dataset<Row> predictions = model.transform(testData);

        predictions.show();

        // Select example rows to display.
       // predictions.select("predictedLabel", "label", "features").show(5);

        // Select (prediction, true label) and compute test error.
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol("indexedLabel")
                .setPredictionCol("prediction")
                .setMetricName("accuracy");
        double accuracy = evaluator.evaluate(predictions);
        System.out.println("Test Error = " + (1.0 - accuracy));

        DecisionTreeClassificationModel treeModel =
                (DecisionTreeClassificationModel) (model.stages()[6]);
        System.out.println("Learned classification tree model:\n" + treeModel.toDebugString());

        System.out.println("cols " +  treeModel.getFeaturesCol());
        System.out.println("root " +  treeModel.rootNode());
        // $example off$

        spark.stop();

//        Dataset<Row> data2 = data.filter(data.col("ACK").equalTo("true"));
//        data2.show();



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












}
