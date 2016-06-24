import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fr.familiar.variable.FeatureModelVariable;

import java.util.Map;
import java.util.Set;

/**
 * Created by macher1 on 07/06/2016.
 */

public class ConfigurationToJSon {

    private FeatureModelVariable _fmv;

    public ConfigurationToJSon (FeatureModelVariable fmv) {
        _fmv = fmv;
    }

    public JsonObject confs2JSON(Map<String, Object> vals) {

        JsonObject r = new JsonObject();
        // if (!_fmv.root().name().equals(ft)) // root is a special case
        vals.keySet().stream().forEach(val -> r.add(val, _convertConfigurationObjectValue(vals.get(val)))); // // r.addProperty(val,
        return r;
    }


    public static JsonElement _convertConfigurationObjectValue(Object o) {
        if (o instanceof Boolean) {
            Boolean b = (Boolean) o ;
            return b ? new JsonPrimitive("true") : new JsonPrimitive("false");
        }
        else if (o instanceof Integer) {
            return new JsonPrimitive((Integer) o);
        }
        return new JsonPrimitive((String) o);


    }

}
