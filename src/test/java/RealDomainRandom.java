import org.chocosolver.solver.search.strategy.selectors.values.RealValueSelector;
import org.chocosolver.solver.variables.RealVar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Created by macher1 on 09/03/2017.
 */

@Deprecated
public class RealDomainRandom implements RealValueSelector {


    private final Random _rand;

    public RealDomainRandom(long seed) {

        this._rand = new Random(seed);
    }

    @Override
    public double selectValue(RealVar var) {
        /*
        ThreadLocalRandom tRandom = ThreadLocalRandom.current();
        tRandom.setSeed(_rand.nextLong());
        double value = tRandom.nextDouble(var.getLB(), var.getUB());*/

        double value = var.getLB() + (var.getUB() - var.getLB()) * _rand.nextDouble();

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
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();

        return truncatedDouble;
    }


}
