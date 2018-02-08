package fr.familiar.experimental.afm;

import fr.familiar.experimental.afm.ArithmeticCompOperator;

/**
 * Created by macher1 on 13/03/2017.
 */
public class AttributedExpression {


    private String _leftVariable;

    public ArithmeticCompOperator get_ao() {
        return _ao;
    }

    private ArithmeticCompOperator _ao;

    public Number get_n() {
        return _n;
    }

    private Number _n;

    public String getLeftVariable() {
        return _leftVariable;
    }

    private String _rightVariable = null;


    public AttributedExpression(String leftVariable, ArithmeticCompOperator ao, double v) {

        _leftVariable = leftVariable;
        _ao = ao;
        _n = v;
    }

    public AttributedExpression(String leftVariable, ArithmeticCompOperator ao, String righVariable) {

        _leftVariable = leftVariable;
        _ao = ao;
        _rightVariable = righVariable;

    }

    public AttributedExpression(String leftVariable, ArithmeticCompOperator ao, int v) {

        _leftVariable = leftVariable;
        _ao = ao;
        _n = v;

    }
}
