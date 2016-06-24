import fr.familiar.interpreter.NSFactory;
import fr.familiar.parser.NameSpace;
import fr.familiar.variable.RType;
import fr.familiar.variable.Variable;
import fr.familiar.variable.VariableIdentifier;
import fr.familiar.variable.VariableImpl;

/**
 * basic domain variable; I specialize for the demo a integer min/max but it can be double as well
 * Created by macher1 on 15/06/2016.
 */
public class IntegerDomainVariable extends VariableImpl {


    protected int _min ;
    protected int _max;

    public IntegerDomainVariable(String name, int min, int max, NameSpace ns) {
        this.name = name;
        this.ns = ns;
        this.vid = new VariableIdentifier(name, ns);
        _min = min;
        _max = max;

    }

    public IntegerDomainVariable(String id, int min, int max) {
        this(id, min, max, NSFactory.mkEmpty());
    }

    @Override
    public RType getRType() {
        return RType.INTEGER; // TODO RType.ATTRIBUTE_DOMAIN
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

    @Override
    public String getSpecificValue() {
        return "[" + _min + ".." + _max + "]";
    }


    public int getMin() {
        return _min;
    }

    public int getMax() {
        return _max;
    }

    // TODO setters
}
