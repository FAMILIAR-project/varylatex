import fr.familiar.variable.FeatureAttribute;
import fr.familiar.variable.FeatureModelVariable;

import java.util.*;

/**
 * Created by macher1 on 08/02/2018.
 */
public class AFMUtils {
    public static Collection<FeatureAttribute> collectAllAttributes(FeatureModelVariable fmv) {

        Map<String,List<FeatureAttribute>> cAttrs = fmv.getFeatureAttributes();
        Set<String> ftWithAttrs
                = cAttrs.keySet();

        Collection<FeatureAttribute> allAttrs = new HashSet<>();
        for (String ft: ftWithAttrs) {
            allAttrs.addAll(cAttrs.get(ft));
        }
        return allAttrs;
    }
}
