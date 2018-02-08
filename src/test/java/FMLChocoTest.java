import fr.familiar.FMLTest;
import fr.familiar.experimental.afm.*;
import fr.familiar.parser.DoubleVariable;
import fr.familiar.variable.FeatureAttribute;
import fr.familiar.variable.FeatureModelVariable;
import fr.familiar.variable.IntegerVariable;
import fr.familiar.variable.Variable;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.criteria.Criterion;
import org.junit.Test;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 *
 * Some basics tests with Choco
 * Created by macher1 on 08/02/2018.
 */
public class FMLChocoTest extends FMLTest {


    private static Logger _log = Logger.getLogger("FMLChocoTest");


    @Test
    public void testFMDemo() throws Exception {

        // INPUT
        FeatureModelVariable fmv = FM ("VARY_LATEX : [VSPACE_FIGURE_TUX] [ACK] [LONG_AFFILIATION]; ACK : [MORE_ACK] [BOLD_ACK]; ");
        fmv.setFeatureAttribute(fmv.getFeature("VSPACE_FIGURE_TUX"), "vspace_tux", new IntegerDomainVariable("", 0, 4)); // TODO: type the attribute
        _log.info(fmv.getFeature("VSPACE_FIGURE_TUX").lookup("vspace_tux").getValue());

        Collection<FeatureAttribute> allAttrs = AFMUtils.collectAllAttributes(fmv);
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

        // TODO FIXME: I don't like this state based solution (side-effect) for counting
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

}
