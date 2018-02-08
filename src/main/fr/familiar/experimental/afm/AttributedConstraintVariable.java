package fr.familiar.experimental.afm;

import fr.familiar.variable.ConstraintVariable;

/**
 * Created by macher1 on 13/03/2017.
 */
public class AttributedConstraintVariable {

    private AttributedExpression _attExpr;

    public AttributedExpression getAttributedConstraint() {
        return _attExpr;
    }



    public AttributedConstraintVariable(AttributedExpression attributedExpression) {
        _attExpr = attributedExpression;
    }
}
