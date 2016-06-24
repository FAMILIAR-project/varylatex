import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.familiar.FMLTest;
import fr.familiar.interpreter.FMLBasicInterpreter;
import fr.familiar.variable.Comparison;
import fr.familiar.variable.FeatureModelVariable;
import fr.familiar.variable.SetVariable;
import fr.familiar.variable.Variable;
import org.junit.Ignore;
import org.junit.Test;
import org.xtext.example.mydsl.fML.SliceMode;


import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by macher1 on 07/06/2016.
 */
public class FMLCrashTest extends FMLTest {

    private static Logger _log = Logger.getLogger("FMLCrashTest");

    @Ignore
    @Test
    public void test1() throws Exception {

        FMLBasicInterpreter fmlI = new FMLBasicInterpreter();
        Variable fmv1 = fmlI.eval("fm1 = FM (A : [B] [C];)");
        assertTrue(fmv1 instanceof FeatureModelVariable);

        FeatureModelVariable fmv = (FeatureModelVariable) fmv1;
        assertEquals(4, fmv.configs().size());

    }



    @Test
    public void test2() throws Exception {

        Variable fmv1 = FM("A : [B] [C] [D] ; ");
        assertTrue(fmv1 instanceof FeatureModelVariable);

        FeatureModelVariable fmv = (FeatureModelVariable) fmv1;
        assertEquals(8, fmv.configs().size());

        FeatureModelVariable fmv2
                = fmv.slice(SliceMode.INCLUDING, fmv.features().names());

        assertEquals(Comparison.REFACTORING, fmv2.compare(fmv));

    }


    @Test
    public void testFM2() throws Exception {
        FeatureModelVariable fmv1 = FM("VARY_LATEX : [ACK] [LONG_AFFILIATION]; ACK : [MORE_ACK] [BOLD_ACK]; ");
        FeatureModelVariable fmv2
                = fmv1.slice(SliceMode.INCLUDING, fmv1.features().names());

        assertNotNull(fmv2);
    }

    @Test
    public void testFM() throws Exception {
        FeatureModelVariable fmv1 = FM("VARY_LATEX : [ACK] [LONG_AFFILIATION]; ACK : [MORE_ACK] [BOLD_ACK]; ");
        assertEquals(10, fmv1.counting(), 0.0);


        Collection<Set<String>> scfs = new HashSet<Set<String>>();


        Set<Variable> cfs = fmv1.configs();
        ConfigurationToJSon convJson = new ConfigurationToJSon(fmv1);
        for (Variable cf : cfs) {
            // configuration to JSON
            SetVariable cfFt = (SetVariable) cf;
            assertNotNull(cfFt);


            Map<String, Object> orderedConf = new ConfigurationToMap(fmv1).confs2map(((SetVariable) cf).names());

            JsonObject confjs = convJson.confs2JSON(orderedConf);
            _log.info("" + confjs);
        }

        // TODO: compare to eg

        JsonObject conf = new JsonParser().parse("{\n" +
                "  \"LONG_AFFILIATION\": true,\n" +
                "  \"ACK\": true,\n" +
                "  \"MORE_ACK\": true,\n" +
                "  \"BOLD_ACK\": false\n" +
                "}").getAsJsonObject();



    }



}
