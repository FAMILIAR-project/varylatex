import fr.familiar.fm.converter.FeatureModelToExpression;
import fr.familiar.parser.DoubleVariable;
import fr.familiar.variable.*;
import gsd.synthesis.Expression;
import gsd.synthesis.ExpressionType;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;
import java.util.logging.Logger;

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.*;
import static org.chocosolver.solver.constraints.nary.cnf.LogOp.and;

/**
 * Created by macher1 on 09/03/2017.
 * TODO: constraints between attributes/attributes and features/attributes
 */
public class FMLChocoModel {

    private static Logger _log = Logger.getLogger("FMLChocoModel");


    public Model transform(FeatureModelVariable fmv) {

        Model model = new Model("" + fmv.root());



        /*
         * FIRST, we add variables:
         *  * features are Boolean variables with domain 0, 1;
         *  * attributes are typically integer/real variables with domain (min, max) or a fixed value
         */
        Set<String> ftNames = fmv.features().names();
        Map<String, List<FeatureAttribute>> atts = fmv.getFeatureAttributes();

        for (String ft : ftNames) {
            BoolVar b = model.boolVar("" + ft);
            if (!atts.containsKey(ft)) // no attributes associated
                continue;


            List<FeatureAttribute> ftAtts = atts.get(ft);


            //FeatureVariable ftA = fmv.getFeature(ft);
            //Collection<Variable> ftAattrs = ftA.getAttributes() ;
            //for (Variable ftAttrVal : ftAattrs) {

            for (FeatureAttribute ftA : ftAtts) {
                FeatureVariable ftv = ftA.getFt();
                String attName = ftA.getName();
                Variable ftAttrVal = ftA.getValue();

                if (ftAttrVal instanceof IntegerDomainVariable) {

                    IntegerDomainVariable domainInt = (IntegerDomainVariable) ftAttrVal;
                    int min = domainInt.getMin();
                    int max = domainInt.getMax();
                    model.intVar(attName, min, max);

                }
                else if (ftAttrVal instanceof DoubleDomainVariable) {



                    DoubleDomainVariable domainDouble = (DoubleDomainVariable) ftAttrVal;

                    double precision = domainDouble.getPrecision();

                    double min = domainDouble.getMin() * precision;
                    double max = domainDouble.getMax() * precision;



                    // IntVar iVar =
                    model.intVar(attName, (int) min, (int) max);
                    // double precision = 0.001d;
                    // RealVar rvar =
                    //        model.realIntView(iVar, precision);

                    // model.intScaleView()

                    //model.realVar("" + attName, min, max, 0.01);
                }
                else if (ftAttrVal instanceof DoubleVariable) {
                    model.realVar(attName, ((DoubleVariable) ftAttrVal).getDouble());

                }
                else if (ftAttrVal instanceof IntegerVariable) {
                    model.intVar(attName, ((IntegerVariable) ftAttrVal).getV());
                }
                else {
                    _log.warning("Unknown attribute type" + ftAttrVal);
                }
            }
        }

        // TODO FIXME
        model.boolVar("SYNTETIC_ROOT_FEATURE", true);




        /*
         * SECOND, we add constraints
         *  including dependencies hierarchy, variability information, and cross-tree constraints
         */

        Collection<Expression<String>> csts = new FeatureModelToExpression(fmv).convert() ;
        for (Expression cst : csts) {
            ILogical log  = _mkLogOp(cst, model);
            if (log == null) {
                _log.warning("cst " + cst);
                continue;
            }
            if (log instanceof LogOp) {
                model.addClauses((LogOp) log);
            }
            else  {
                assert (log instanceof BoolVar);
                BoolVar b = (BoolVar) log;
                model.addClauses(ifOnlyIf(b, model.boolVar(true)));
            }
        }

        /*BoolVar[] vars = model.retrieveBoolVars();
        for (int i = 0; i < vars.length; i++)
            _log.warning("vars " + vars[i]);*/

        return model;

    }

    /*
     * transform a constraint (FML) into a constraint in the CSP model
     */
    private ILogical _mkLogOp(Expression e, Model model) {
        if (e.getType() == ExpressionType.FEATURE) {
            // if (e.getFeature().toString().equals("SYNTETIC_ROOT_FEATURE")) // TODO weird
               // return model.boolVar(true);
            if (e.getFeature().toString().equals("1"))
                return model.boolVar(true);
            if (e.getFeature().toString().equals("0"))
                return model.boolVar(false);
            BoolVar bv = retrieveBoolVarByFtName(model, e.getFeature().toString());
            if (bv == null) {
                _log.warning("\n Unknown boolean variable\n");
            }
            return bv;
        }
        else if (e.getType() == ExpressionType.TRUE) {
            return model.boolVar(true);
        }
        else if (e.getType() == ExpressionType.FALSE) {
            return model.boolVar(false);
        }
        else if (e.getType() == ExpressionType.NOT) {
           ILogical l = _mkLogOp(e.getLeft(), model);
           return nand(l);
        }
        else {
            ILogical l = _mkLogOp(e.getLeft(), model);
            ILogical r = _mkLogOp(e.getRight(), model);

            assert (l != null && r != null);

            if (e.getType() == ExpressionType.IMPLIES) {
                return implies(l, r);
            }
            else if (e.getType() == ExpressionType.OR) {
                return or(l, r);
            }
            else if (e.getType() == ExpressionType.AND) {
                return and(l, r);
            }
            else if (e.getType() == ExpressionType.IFF) {
                return ifOnlyIf(l, r);
            }

        }
        // FIXME
        return null;
    }

    /*
     * the boolean variable corresponding to a feature
     * we could use an internal map to store feature->variable mappig
     */
    private BoolVar retrieveBoolVarByFtName(Model model, String s) {
        BoolVar[] bvs = model.retrieveBoolVars();
        for (int i = 0; i < bvs.length; i++) {
            BoolVar bv = bvs[i];
            if (bv.getName().equals(s))
                return bv;
        }
        return null;
    }
}
