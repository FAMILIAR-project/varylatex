package fr.familiar.experimental.afm;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.selectors.values.RealValueSelector;
import org.chocosolver.solver.variables.RealVar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by macher1 on 09/03/2017.
 */


public class RealDomainRandom implements RealValueSelector {


    private final Random _rand;

    public RealDomainRandom(long seed) {

        this._rand = new Random(seed);
    }

    @Override
    public double selectValue(RealVar var) {

        //ThreadLocalRandom tRandom = ThreadLocalRandom.current();
     //   tRandom.setSeed(_rand.nextLong());
       // double value = tRandom.nextDouble(var.getLB(), var.getUB());

        double value = var.getLB() + (var.getUB() - var.getLB()) * _rand.nextDouble();

        System.err.println("up=" + var.getUB() + " low=" + var.getLB() + " value=" + value);
        /*
        We do not have nextValue in RealVar
        int i = rand.nextInt(var.getDomainSize());
        int value = var.getLB();
        while (i > 0) {
            value = var.nextValue(value);
            i--;
        }
        return value;*/


        Double truncatedDouble = BigDecimal.valueOf(value)
                // .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();

        /*
        double next = _rand.nextDouble();
        System.err.println("RANDOMMMM " + next);


        double low = var.getLB();
        if (low == Double.NEGATIVE_INFINITY) low = -Double.MAX_VALUE;
        double upp = var.getUB();
        if (upp == Double.POSITIVE_INFINITY) upp = Double.MAX_VALUE;
        double r = low + (upp - low) * next; //(low + upp) / 2.0;
        if (r <= low || r >= upp) {
            throw new SolverException("RealDomainMiddle: find a value outside current domain!");
        }
        return r;*/


       return truncatedDouble;
    }


}
