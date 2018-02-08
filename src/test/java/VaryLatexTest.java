import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.familiar.FMLTest;
import fr.familiar.experimental.afm.*;
import fr.familiar.parser.DoubleVariable;
import fr.familiar.variable.*;
import fr.familiar.variable.Variable;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.DecisionTreeClassificationModel;
import org.apache.spark.ml.classification.DecisionTreeClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.criteria.Criterion;
import org.junit.Test;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.FileSystemTemplateLocator;

import java.io.*;
import java.util.*;
import java.util.Random;
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
        RealVar fakeR = model.realVar("fakeR", 2.2, 5.2, 0.01d);

        // model.post(new RealConstraint("fakeR", ">", new FixedRealVarImpl("28", 2.8, model)));


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

        solver.setSearch(

                // fr.familiar.experimental.afm.RealVarSearch.randomSearch(new RealVar[] { fakeR }, new Random().nextLong()),
               Search.randomSearch(
                               new IntVar[] { ACK, stretch, LONG_AFFILIATION, vspace }, new Random().nextLong()),
                Search.realVarSearch(new org.chocosolver.solver.search.strategy.selectors.variables.Random<>(r.nextLong()), new RealDomainRandom(l), fakeR)

                //Search.randomSearch(
    //                        new IntVar[] { vspace }, new Random().nextLong())

                );


        Criterion cr;
        List<Solution> allSols = solver.findAllSolutions(new SolutionCounter(model, 1000));
        for (Solution sol : allSols) {
            double[] fakeB  = sol.getRealBounds(fakeR);
            System.err.println("" + fakeB[0] + " " + fakeB[1]);

        }
        // System.err.println("" + allSols);

       /* int MAX_SOL = 100;
        int nSol = 0;
        while(solver.solve()) {
            // do something, e.g. print out variable values
            //        solver.showStatistics();
            // solver.showSolutions();
            Solution sol = new Solution(solver.getModel());
            sol.record();
            System.out.println("sol=" + sol);
            solver.setRestartOnSolutions();
            if (nSol++ > MAX_SOL)
                break;
        }*/


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
            //fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "vspace_tux", new fr.familiar.experimental.afm.IntegerDomainVariable("", 5, 10)); // TODO: type the attribute
            //fmv.setFeatureAttribute(fmv.getFeature("FIGURE_TUX"), "size_tux", new fr.familiar.experimental.afm.DoubleDomainVariable("", 3.0, 5.0, 10.0)); // TODO: type the attribute


            FMLChocoSolver fmlChocoSolver = new FMLChocoSolver(fmv);
            Collection<FMLChocoConfiguration> cfgs = fmlChocoSolver.configsALL();
            // Collection<fr.familiar.experimental.afm.FMLChocoConfiguration> cfgs = new fr.familiar.experimental.afm.FMLChocoSolver(model, fmv).configs((int) fmv.counting());
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
        //Collection<fr.familiar.experimental.afm.FMLChocoConfiguration> cfgs = fmlChocoSolver.configs(3);
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
    public void testX264() throws Exception {


        String TARGET_FOLDER = "outputX264";

        //String trailerLocation = "/Users/macher1/Documents/SANDBOX/x264Expe/sintel_trailer_2k_480p24.y4m";

        // Warning: the "../" was before
        String trailerLocation =  "forest_jester-dv.mov"; //"gothism-dv.mov"; //"bridge_close_cif.y4m"; //"bridge_far_cif.y4m"; //"Netflix_Boat_4096x2160_60fps_8bit_420.y4m"; //"Mobile_ProRes.mov"; //"flower_sif.y4m"; //"students_cif.y4m"; //"riverbed_1080p25.y4m"; //"sunflower_1080p25.y4m"; //"ice_cif.y4m"; //"husky_cif.y4m"; //"tennis_sif.y4m"; // "waterfall_cif.y4m"; // "football_cif_15fps.y4m" ; //"flower_sif.y4m" ; // "claire_qcif.y4m"; // "football_cif.y4m"; //"akiyo_qcif.y4m"; //"FourPeople_1280x720_60.y4m"; //"blue_sky_1080p25.y4m"; //"720p50_parkrun_ter.y4m";//"tractor_1080p25.y4m"; //"soccer_4cif.y4m"; //"deadline_cif.y4m"; //"tos3k.y4m"; //"football_cif.y4m" ; // "Coastguard_H264.mp4"; // "spiderman.mp4" ;//"sintel_trailer_2k_480p24.y4m"; //"mobile_sif_mono.y4m"; //"crowd_run_1080p50.y4m";  //"mobile_sif_mono.y4m"; //"mobile_sif.y4m"; //"ducks_take_off_420_720p50.y4m"; // "News_H264.mp4"; //"crowd_run_1080p50.y4m" ; // "sintel_trailer_2k_480p24.y4m"; //"spiderman.mp4"; // "elephantsdream_source.264"; //"football_422_cif.y4m"; // "sintel_trailer_2k_480p24.y4m"; //"akiyo_qcif.y4m"; // //"flower_sif.y4m"; //"football_cif.y4m"; //"paris_cif.y4m"; //"students_cif.y4m"; //"eledream_640x360_128.y4m"; //"highway_cif.y4m"; //"garden_sif.y4m"; //"foreman_cif.y4m"; //"crowd_run_1080p50.y4m"; //"tennis_sif.y4m" ; //"husky_cif.y4m"; //"akiyo_qcif.y4m";  //"football_cif_15fps.y4m"; //"flower_sif.y4m"; // "waterfall_cif.y4m" ; // "mobile_cif.y4m"; //"football_cif_15fps.y4m"; // football_cif.y4m"; // "sintel_trailer_2k_480p24.y4m"; // "ice_cif.y4m"; //"football_cif.y4m"; // "sintel_trailer_2k_480p24.y4m" ; // "akiyo_qcif.y4m"; // "football_cif_15fps.y4m"; //"husky_cif.y4m"; //"../sign_irene_qcif.y4m"; //"../football_cif_15fps.y4m"; //"../flower_sif.y4m"; // "../claire_qcif.y4m" ; // "../akiyo_qcif.y4m" ; //"../sympsons.mp4" ; // "../elephantsdream_source.264" ; // "../sintel_trailer_2k_480p24.y4m";

        // TODO: cp x264 to /srv/local as well?
        String X264cmdLocation = "../x264/x264"; //"../x264-custom/x264/x264";    // "x264" ; // "/Users/macher1/Documents/SANDBOX/x264Expe/x264-r2851-ba24899"; //"x264"; // x264/x264

        String oExtension = "264";

        final int REPEAT = 5;
        boolean WITH_ASM = true; // default: no_asm

        String benchName = "bench3";

// _ because "-" is not allowed in FALILIAR (I think coz of FeatureIDE limitation

        FeatureModelVariable fmv = FM(
                "H264 : no_asm [no_8x8dct] [no_cabac] [no_deblock] [no_fast_pskip] [no_mbtree]\n" +
                        "            [no_mixed_refs] [no_weightb] ref rc_lookahead ; ref : (ref1|ref5|ref9); rc_lookahead :\n" +
                        "            (rc_lookahead20 | rc_lookahead40 | rc_lookahead60); ");

        /* WITH_ASM=true
        FeatureModelVariable fmv = FM(
                "H264 : [no_asm] [no_8x8dct] [no_cabac] [no_deblock] [no_fast_pskip] [no_mbtree]\n" +
                        "            [no_mixed_refs] [no_weightb] ref rc_lookahead ; ref : (ref1|ref5|ref9); rc_lookahead :\n" +
                        "            (rc_lookahead20 | rc_lookahead40 | rc_lookahead60); !no_asm; ");*/
        assertEquals(1152, fmv.counting(), 0.0);
        // TODO "set" variable (numerical) != domain

       // mkHeaders



        Set<Variable> cfs = fmv.configs();
        Collection<Set<String>> scfs = new HashSet<Set<String>>();
        for (Variable cf : cfs) {
            Set<String> confFts = ((SetVariable) cf).names();
            scfs.add(confFts);

        }


        assertEquals(scfs.size(), cfs.size());
        assertEquals(1152, cfs.size());


        Set<String> fts = fmv.features().names();
        fts.remove("rc_lookahead20");
        fts.remove("rc_lookahead40");
        fts.remove("rc_lookahead60");

        // what a HACK!
        fts.remove("ref1");
        fts.remove("ref5");
        fts.remove("ref9");

        // both sorted
        String strCSV = "";
        strCSV = "configurationID" + "," + fts.stream().sorted().collect(Collectors.joining(",")) + "\n";

        String fullHeader = "configurationID" + "," + fts.stream().sorted().collect(Collectors.joining(",")) + ",size,usertime,systemtime,elapsedtime" + "";

        int idConf = 0;
        // FileWriter fw =
        for (Set<String> cf : scfs) {
            idConf++;
            Map<String, Object> conf =  _mkConfMap(fmv, cf, WITH_ASM); // easier to handle configuration object

            // toCSV line
            String str = conf.keySet().stream().sorted().map(ft -> conf.get(ft).toString()).collect(Collectors.joining(","));
            String csvLine = "" + idConf + "," + str;
            strCSV += csvLine + "\n";

            // toParams line
            String strParam = "";
            Set<String> params = conf.keySet();
            for (String param : params) {
                Object val = conf.get(param);
                if (val instanceof Boolean) {
                    boolean b = ((Boolean) val).booleanValue();

                    if (b) {
                        strParam += _mkParam(param) + " ";
                    }
                }
                else {
                    int v = Integer.parseInt((String) val);
                    strParam += _mkParam(param) + " " + v + " ";
                }

            }

           // strParam += " --trellis 0";
//   // "(gtime -f \"USERTIME %U\\nSYSTEMTIME %S\\nELAPSEDTIME %e\\nMEMORYTIME %K\" x264 --no-asm --ref 5 --no-fast-pskip --no-8x8dct --no-deblock --rc-lookahead 40  --no-cabac --no-weightb --no-mixed-refs  -o sintel$numb.flv sintel_trailer_2k_480p24.y4m) 2> $logfilename\n" +

            String shR = "#!/bin/bash\n\n" +
                    "numb='" + idConf + "'\n" +
                    "logfilename=\"/srv/local/macher/" + benchName + "/output/$numb.log\"\n" +
                    "trailerlocation='" + "/srv/local/macher/" + benchName + "/" + trailerLocation + "'" +
                    //"trailerlocation='" + trailerLocation + "'" +
                    "\n" +
                     "\n" +
                    "TIMEFORMAT=\"USERTIME %U                                                                                            \n" +
                    "SYSTEMTIME %S                                                                                                      \n" +
                    "ELAPSEDTIME %R\"; { time " +
                    // "(gtime -f \"USERTIME %U\\nSYSTEMTIME %S\\nELAPSEDTIME %e\\nMEMORYTIME %K\" " +
                    //"x264 "
                    //"../x264/x264 "
                    X264cmdLocation + " "
                            + strParam +
                            " -o /srv/local/macher/" + benchName + "/tempvids/sintel$numb" + "." + oExtension + " $trailerlocation ; } 2> $logfilename\n" +
                        //    " -o sintel$numb" + ".flv $trailerlocation) 2> $logfilename\n" +
                    "# size of the video\n" +
                   // "size=`du sintel$numb." + oExtension + " | cut -f1`\n" +
                    "size=`ls -lrt /srv/local/macher/" + benchName + "/tempvids/sintel$numb." + oExtension + " | awk '{print $5}'`\n" +
                    "# analyze log to extract relevant timing information\n" +
                    "usertime=`grep \"USERTIME\" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`\n" +
                    "systemtime=`grep \"SYSTEMTIME\" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`\n" +
                    "elapsedtime=`grep \"ELAPSEDTIME\" $logfilename | sed 's/[^.,0-9]*//g ; s/,/./g'`\n" +
                    // "memorytime=`grep \"MEMORYTIME\" $logfilename | sed 's/[^.0-9]*//g'`\n" +
                    "# clean\n" +
                    "rm /srv/local/macher/" + benchName + "/tempvids/sintel$numb." + oExtension + "\n" +
                    "\n" +
                    "\n" +
                    "csvLine=" + "'" + csvLine + "'" + "\n" +
                    //"csvLine=\"$csvLine,$size,$usertime,$systemtime,$elapsedtime,$memorytime\""
                    "csvLine=\"$csvLine,$size,$usertime,$systemtime,$elapsedtime\""
                    + "\n"
                    + "echo $csvLine"
                    ;
            System.err.println(shR);
            // (gtime -f "\t%U user,\t%S system,\t%e elapsed,\t%K"
            //System.err.println("(gtime -f \"USERTIME %U\\nSYSTEMTIME %S\\nELAPSEDTIME %e,\\nmemory %K\" x264 " + strParam + " -o sintel" + idConf + ".flv sintel_trailer_2k_480p24.y4m) 2> " + idConf + ".log");
            //String size = "du -k sintel" + idConf + ".flv | cut -f1";
            // System.err.println("" + size);
            System.err.println("" + csvLine);


            FileWriter fw = new FileWriter(new File(TARGET_FOLDER + "/" + idConf + ".sh"));
            fw.write(shR);
            fw.close();

           // fw.write(str + "\n");
           // fw.close();
        }



        FileWriter fwAll = new FileWriter(new File(TARGET_FOLDER + "/" + "launchAll")); // not "sh" since we seek sh files ;)
        /*fwAll.write("#!/bin/bash\n\n" +
                "header=" + "'" + fullHeader + "'" + "\n" +
                "x64configs=`ls *.sh`\n" +
                "touch x264-results.csv\n" +
                "cat /dev/null > x264-results.csv\n" +
                "echo \"$header\" > x264-results.csv\n" +
                "for x264config in $x64configs\n" +
                "do\n" +
                "   echo \"Processing: \" $x264config\n" +
                "   csvLine=`bash $x264config`\n" +
                "   echo \"$csvLine\" >> x264-results.csv\n" +
                "   csv=\"$csv$csvLine\\n\"" + "\n" +
                "done\n"
                // "echo \"$header\\n$csv\" 2> x264-results.csv"
        );*/

        fwAll.write("#!/bin/bash\n\n" +
                        // specific to IGRIDA: copy to /srv/local/macher/
                        // new trailer location is then /srv/local/macher/trailerlocation
                        "echo \"Copying video: \" " + trailerLocation + "\n" +
                        "mkdir -p /srv/local/macher/\n" +
                        "mkdir -p /srv/local/macher/" + benchName + "/\n" +
                        "mkdir -p /srv/local/macher/" + benchName+ "/output/\n" + // log location
                        "rm -f /srv/local/macher/" + benchName + "/output/*.log\n" + // in case
                        "mkdir -p /srv/local/macher/" + benchName + "/tempvids/\n" + // temporary videos
                        "cp ../" + trailerLocation + " /srv/local/macher/" + benchName + "/" + "\n" +
                        "header=" + "'" + fullHeader + "'" + "\n" +
                        "x64configs=`ls *.sh`\n" +
                        "for i in {1.." + REPEAT + "}\n" +
                        "do\n" +
                        "csvOutput=\"x264-results$i.csv\"\n" +
                        "touch $csvOutput\n" +
                        "cat /dev/null > $csvOutput\n" +
                        "echo \"$header\" > $csvOutput\n" +
                        "for x264config in $x64configs\n" +
                        "do\n" +
                        "echo \"Processing: \" $x264config\n" +
                        "   csvLine=`bash $x264config`\n" +
                        "   echo \"$csvLine\" >> $csvOutput\n" +
                        "done\n" +
                        "tar cvf \"oX264-results$i.tar.gz\" /srv/local/macher/" + benchName + "/output/*.log\n" +
                        "done\n" +
                        "rm /srv/local/macher/" + benchName + "/" + trailerLocation + "\n" +
                        "rm -rf /srv/local/macher/" + benchName + "/" + "\n" +
                        "echo \"Deleting bench folder\"" + "\n"
                // "echo \"$header\\n$csv\" 2> x264-results.csv"
        );
        fwAll.close();

        FileWriter fw = new FileWriter(new File(TARGET_FOLDER + "/" + "x264.csv"));
        fw.write(strCSV);
        fw.close();



        //fmv.setFeatureAttribute(fmv.getFeature("H264"), "lookahead", new fr.familiar.experimental.afm.IntegerDomainVariable("", 5, 10)); // TODO: type the attribute
        //fmv.setFeatureAttribute(fmv.getFeature("H264"), "ref", new fr.familiar.experimental.afm.DoubleDomainVariable("", 3.0, 5.0, 100.0)); // TODO: type the attribute


        /*Collection<fr.familiar.experimental.afm.AttributedConstraintVariable> cstsAtts = new HashSet<>();
        cstsAtts.add(new fr.familiar.experimental.afm.AttributedConstraintVariable(new fr.familiar.experimental.afm.AttributedExpression("vspace_tux", ArithmeticCompOperator.GE, 7)));
        cstsAtts.add(new fr.familiar.experimental.afm.AttributedConstraintVariable(new fr.familiar.experimental.afm.AttributedExpression("size_tux", ArithmeticCompOperator.GE, 4.9)));




        fr.familiar.experimental.afm.FMLChocoSolver fmlChocoSolver = new fr.familiar.experimental.afm.FMLChocoSolver(fmv, cstsAtts);
        Collection<fr.familiar.experimental.afm.FMLChocoConfiguration> cfgs = fmlChocoSolver.configsALL();
        //Collection<fr.familiar.experimental.afm.FMLChocoConfiguration> cfgs = fmlChocoSolver.configs(3);
        int c = 0;
        for (fr.familiar.experimental.afm.FMLChocoConfiguration cfg : cfgs) {
            _log.info("cfg (" + c++ + ") = " + cfg.getValues());
        }*/

        /*
        Solver solver = fmlChocoSolver.getCurrentSolver();
        // side-effect
        // works because getSolutionCount() returns the number of solving you have made (basically number of calls to "solve" / findSolution)
        double solutionCount = (double) solver.getSolutionCount();
        _log.warning("solutionCount " + solutionCount);
        assertEquals(fmv.counting(), solutionCount, 0.0);
        assertEquals(fmv.counting(), cfgs.size(), 0.0);*/


    }

    private String _mkParam(String param) {
        if (param.equals("H264"))
            return "";
        String encParam = param.replace("_", "-");
        return "--" + encParam + "";
    }

    private Map<String, Object> _mkConfMap(FeatureModelVariable fmv, Set<String> cf, boolean wasm) {

        Map<String, Object> lConf = new HashMap<>();


        Set<String> allFts = fmv.features().names();
        for (String ft : allFts) {

            if (ft.startsWith("rc_lookahead") && !ft.equals("rc_lookahead")) { // TODO: hack!
                // eg rc_lookahead20
                if (cf.contains(ft)) {
                    String val = ft.substring("rc_lookahead".length());
                    lConf.put("rc_lookahead", val);
                }
            }

            else if (ft.startsWith("ref")) { // TODO: hack!
                if (ft.equals("ref1") || ft.equals("ref5") || ft.equals("ref9")) {
                    // eg rc_lookahead20
                    if (cf.contains(ft)) {
                        String val = ft.substring("ref".length());
                        lConf.put("ref", val);
                    }
                }
            }

            else if (ft.equals("ref")) { // TODO: hack!
               // nothing
            }

            else if (ft.equals("rc_lookahead")) { // TODO: hack!
                // nothing
            }


            // HACK (sorry)
            // configuration IDs have been generated with an hash map that
            // itself relies on the order of configuration values
            // I cannot set no_asm as optional and add a negated constraint (it basically changes the order
            // and thus the configuration IDs
            else if (wasm && ft.equals("no_asm")) {
                lConf.put(ft, false); // we negate!
            }

            else {
                if (cf.contains(ft))
                    lConf.put(ft, true);
                else
                    lConf.put(ft, false);
            }
        }

        return lConf;
    }




    @Test
    public void test2() throws Exception {

        String TARGET_FOLDER = "output";

        Logger.getLogger("fr.familiar.experimental.afm.ConfigurationToMap").setLevel(Level.WARNING);
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
           // Map<String, Object> orderedConf = new fr.familiar.experimental.afm.ConfigurationToMap(fmv).populateAttributeValuesAndConfs2map(cf);

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
        Collection<FMLChocoConfiguration> scfs = new FMLChocoSolver(fmv).configs(400);

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
