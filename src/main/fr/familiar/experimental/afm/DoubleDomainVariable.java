package fr.familiar.experimental.afm;

import fr.familiar.interpreter.FMLShell;
import fr.familiar.interpreter.NSFactory;
import fr.familiar.interpreter.VariableNotExistingException;
import fr.familiar.parser.NameSpace;
import fr.familiar.variable.RType;
import fr.familiar.variable.Variable;
import fr.familiar.variable.VariableIdentifier;
import fr.familiar.variable.VariableImpl;

import java.util.Collection;

/**
 * Created by macher1 on 24/06/2016.
 */
public class DoubleDomainVariable extends VariableImpl {

    protected double _min ;
    protected double _max;


    public final static double PRECISION_DEFAULT = 100;
    protected double _precision;

    public DoubleDomainVariable(String name, double min, double max, NameSpace ns) {

        this(name, min, max, PRECISION_DEFAULT, ns);


    }

    public DoubleDomainVariable(String name, double min, double max) {
        this(name, min, max, NSFactory.mkEmpty());
    }

    public DoubleDomainVariable(String name, double min, double max, double precision) {
        this(name, min, max, precision, NSFactory.mkEmpty());
    }

    public DoubleDomainVariable(String name, double min, double max, double precision, NameSpace ns) {
        this.name = name;
        this.ns = ns;
        this.vid = new VariableIdentifier(name, ns);
        _min = min;
        _max = max;
        _precision = precision;
    }


    @Override
    public RType getRType() {
        return RType.DOUBLE; // TODO fixme
    }


    @Override
    public String getSpecificValue() {
        return "[" + _min + ".." + _max + "]";
    }

    @Override
    public Variable copy() {
        // TODO
        return null;
    }

    @Override
    public void setValue(Variable variable) {
        // TODO
    }

    public double getMin() {
        return _min;
    }

    public double getMax() {
        return _max;
    }

    public double getPrecision() {
        return _precision;
    }
}
