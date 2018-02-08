package fr.familiar.experimental.afm;

import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.RealValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;

/**
 * Created by macher1 on 09/03/2017.
 */
@Deprecated
public class RealVarSearch {



    /**
     * a failed experience
     * Mathieu Acher (adaptation from Search.randomSearch() for IntVar
     * Randomly selects a variable and assigns it to a value randomly taken in
     * the domain with a given (TODO) precision
     *
     * @param vars list of variables
     * @param seed a seed for random
     * @return assignment strategy
     */
    @Deprecated
    public static RealStrategy randomSearch(RealVar[] vars, long seed) {


        RealValueSelector value = new RealDomainRandom(seed);
        // RealValueSelector bound = new RealDomainRandomBound(seed);
        RealValueSelector selector = var -> {
             return
                     value.selectValue(var);
        };
        return Search.realVarSearch(new Random<>(seed), selector, vars);
    }




}
