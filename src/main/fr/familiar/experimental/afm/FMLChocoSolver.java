package fr.familiar.experimental.afm;

import fr.familiar.variable.FeatureAttribute;
import fr.familiar.variable.FeatureModelVariable;
import fr.familiar.variable.Variable;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.impl.FixedBoolVarImpl;
import org.chocosolver.solver.variables.view.BoolNotView;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by macher1 on 09/03/2017.
 */
public class FMLChocoSolver {


    private Logger _log = Logger.getLogger("fr.familiar.experimental.afm.FMLChocoSolver");

    private Collection<AttributedConstraintVariable> _cstsAtts = new HashSet<>();
    private FeatureModelVariable _fmv ;
    private Model _model;

    public FMLChocoSolver(FeatureModelVariable fmv) {

        _fmv = fmv;

    }

    public FMLChocoSolver(FeatureModelVariable fmv, Collection<AttributedConstraintVariable> cstsAtts) {
        // TODO weird FIX ME
        // we should add a method addConstraint into fmv
        _fmv = fmv;
        _cstsAtts = cstsAtts;
    }


    public Collection<FMLChocoConfiguration> configs(int max) {

        _model = new FMLChocoModel().transform(_fmv, _cstsAtts);

        Solver solver = _model.getSolver();


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



        Collection<AbstractStrategy> strategies = new HashSet<>();


        //TODO: random search does not exist for real!
        /*
        RealVar[] rVars = _model.retrieveRealVars();
        for (int i = 0; i < rVars.length; i++) {
            strategies.add(fr.familiar.experimental.afm.RealVarSearch.randomSearch(new RealVar[] { rVars[i] }, r.nextLong()));
        }*/

        IntVar[] biVars = _model.retrieveIntVars(true);
        for (int i = 0; i < biVars.length; i++) {
            strategies.add(Search.randomSearch(
                    new IntVar[] { biVars[i] }, r.nextLong()));
        }

        /*solver.setSearch(Search.randomSearch(
                biVars, r.nextLong()));*/

       solver.setSearch(strategies.toArray(new AbstractStrategy[strategies.size()]));

        // solver.setRestartOnSolutions();
        Collection<FMLChocoConfiguration> cfgs = new HashSet<>();

        int nSol = 0;
        while (solver.solve()) {
            if (nSol++ > max)
                break;


            Solution sol = new Solution(solver.getModel()) ; // solver.findSolution();
            sol.record();
            if (sol ==  null) // WEIRD TODO
                break;
            FMLChocoConfiguration cfg = mkConfiguration(sol, _model);
            cfgs.add(cfg);

            solver.setRestartOnSolutions();

        }

        return cfgs;
    }

    public Collection<FMLChocoConfiguration> configsALL() {

        _model = new FMLChocoModel().transform(_fmv,  _cstsAtts);

        Solver solver = _model.getSolver();
        Collection<FMLChocoConfiguration> cfgs = new HashSet<>();

        List<Solution> sols = solver.findAllSolutions();
        for (Solution sol : sols) {
            FMLChocoConfiguration cfg = mkConfiguration(sol, _model);
            // treat solution
            cfgs.add(cfg);
        }


/*
OLD version: twice iteration
        while (solver.solve()) {
            Solution sol = solver.findSolution();

            if (sol ==  null) // WEIRD TODO
                break;
            fr.familiar.experimental.afm.FMLChocoConfiguration cfg = mkConfiguration(sol, _model);
            // treat solution
            cfgs.add(cfg);
        }*/

        return cfgs;
    }

    private FMLChocoConfiguration mkConfiguration(Solution cf, Model model) {

       // _log.warning("cf " + cf);

        FMLChocoConfiguration lConf = new FMLChocoConfiguration();

        org.chocosolver.solver.variables.Variable[] vars = model.getVars();
        for (int i = 0; i < vars.length; i++) {
            org.chocosolver.solver.variables.Variable var  = vars[i];

            if (var instanceof FixedBoolVarImpl) {
                // true or false constants
                // ignore
            }
            else if (var instanceof BoolVar) {
                BoolVar iVar = (BoolVar) var;
                int vl = cf.getIntVal(iVar);
                String name = var.getName();
                if (iVar.isNot()) { // not (VAR) vary strange Choco model seems to include negate variables as new variables
                    name = ((BoolNotView) iVar).getVariable().getName();
                    if (vl == 1)
                        vl = 0;
                    else
                        vl = 1;
                }
                lConf.put(name, vl == 1);
            }
            else if (var instanceof IntVar) { // IntVar or BoolVar
                int vl = cf.getIntVal((IntVar) var);

                // TODO
                // if the Choco variable corresponds to a Real variable of FML, then we have to divide
                String varName = var.getName();

                boolean isAttributeWithRealDomain = false;
                DoubleDomainVariable correspondingDoubleAttribute = null;
                Collection<FeatureAttribute> attrs = _collectAllAttributes(_fmv);
                for (FeatureAttribute attr : attrs) {
                    if (varName.equals(attr.getName())) {

                        Variable varAttribute = attr.getValue();
                        if (varAttribute instanceof DoubleDomainVariable) {
                            isAttributeWithRealDomain = true;
                            correspondingDoubleAttribute = (DoubleDomainVariable) varAttribute;
                            break;
                        }
                    }
                }

                if (isAttributeWithRealDomain) {

                    double precision = correspondingDoubleAttribute.getPrecision();
                    lConf.put(varName, ((double) vl / precision));   // TODO should depend on the precision of the Double domain
                }
                else
                    lConf.put(varName, vl);
            }
            else if (var instanceof RealVar) {
                double[] vl = cf.getRealBounds((RealVar) var);
//                assert (vl[0] == vl[1]);
                lConf.put(var.getName(), vl[0]); // in our case a fixed value
            }

        }

        return lConf;

    }

    // flatten
    private Collection<FeatureAttribute> _collectAllAttributes(FeatureModelVariable fmv) {

        Collection<FeatureAttribute> attrs = new HashSet<>();
        Map<String, List<FeatureAttribute>> atts = _fmv.getFeatureAttributes();
        Collection<List<FeatureAttribute>> alls = atts.values();
        for (List<FeatureAttribute> a1 : alls) {
            attrs.addAll(a1);
        }
        return attrs;

    }

    public Solver getCurrentSolver() {
        return _model.getSolver();
    }
}
