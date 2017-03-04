import fr.familiar.parser.DoubleVariable;
import fr.familiar.variable.FeatureAttribute;
import fr.familiar.variable.FeatureModelVariable;
import fr.familiar.variable.IntegerVariable;
import fr.familiar.variable.Variable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by macher1 on 15/06/2016.
 */
public class ConfigurationToMap {


    private static Logger _log = Logger.getLogger("ConfigurationToMap");


    private final FeatureModelVariable _fmv;

    public ConfigurationToMap(FeatureModelVariable fmv) {
        _fmv = fmv ;
    }




    /**
     * Given a configuration for Boolean values (true/false), "complete" the configuration with numerical values
     * (we randomly pick a value)
     * @param cf
     * @return
     */
    public Map<String, Object> populateAttributeValuesAndConfs2map(Set<String> cf) {

        Map<String, Object> lConf = new HashMap<>();



        Set<String> allFts = _fmv.features().names();
        for (String ft : allFts) {
            if (cf.contains(ft))
                lConf.put(ft, true);
            else
                lConf.put(ft, false);
        }



        // populate attributes here
        // map of configuration values


        Map<String, List<FeatureAttribute>> attrs = _fmv.getFeatureAttributes();
        Set<String> fts = attrs.keySet();
        for (String ft: fts) {
            List<FeatureAttribute> atts = attrs.get(ft);
            for (FeatureAttribute att : atts ) {
                _log.info(ft + "[@" + att.getName() + "] = " + att.getValue());

                Variable domain = att.getValue();
                if (domain instanceof IntegerDomainVariable) {
                    IntegerDomainVariable dom = (IntegerDomainVariable) domain;
                    int min = dom.getMin();
                    int max = dom.getMax();

                    int confValue = (int) (Math.random() * (max - min)) + min;
                    _log.info("" + confValue);
                    if (!cf.contains(ft)) {
                        _log.info("Setting default value for the attribute: " + min);
                        lConf.put(att.getName(), min);
                    }
                    else {
                        _log.info("Value for the attribute: " + confValue);
                        lConf.put(att.getName(), new Integer(confValue));
                    }


                }

                if (domain instanceof DoubleDomainVariable) {
                    DoubleDomainVariable dom = (DoubleDomainVariable) domain;
                    double min = dom.getMin();
                    double max = dom.getMax();

                    double confValue = (Math.random() * (max - min)) + min;
                    _log.info("" + confValue);
                    if (!cf.contains(ft)) {
                        _log.info("Setting default value for the attribute: " + min);
                        lConf.put(att.getName(), min);
                    }
                    else {
                        _log.info("Value for the attribute: " + confValue);
                        lConf.put(att.getName(), new Double(confValue));
                    }


                }

                if (domain instanceof IntegerVariable) {
                    lConf.put(att.getName(), new Integer(((IntegerVariable) domain).getV()));
                }

                if (domain instanceof DoubleVariable) {
                    lConf.put(att.getName(), new Double(((DoubleVariable) domain).getDouble()));
                }


            }

        }

        return lConf;
    }
}
