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

    public DoubleDomainVariable(String name, double min, double max, NameSpace ns) {
        this.name = name;
        this.ns = ns;
        this.vid = new VariableIdentifier(name, ns);
        _min = min;
        _max = max;

    }

    public DoubleDomainVariable(String id, double min, double max) {
        this(id, min, max, NSFactory.mkEmpty());
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

}
