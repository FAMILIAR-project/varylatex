import java.util.HashMap;
import java.util.Map;

/**
 * Created by macher1 on 09/03/2017.
 */
public class FMLChocoConfiguration {

    private Map<String, Object> _confValues = new HashMap<>();

    public void put(String name, boolean val) {
        _confValues.put(name, val);
    }

    public void put(String name, int val) {
        _confValues.put(name, val);
    }

    public void put(String name, double val) {
        _confValues.put(name, val);
    }

    public Map<String, Object> getValues() {
        return _confValues;
    }
}
