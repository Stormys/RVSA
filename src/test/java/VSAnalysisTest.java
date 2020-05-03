import common.ErrorMessage;
import common.Utils;
import vsa.VSAnalysisTransformer;
import org.junit.Assert;
import org.junit.Test;
import soot.Main;
import soot.PackManager;
import soot.Transform;
import soot.options.Options;

public class VSAnalysisTest extends AnalysisTest {
    void add_analysis() {
        analysisName = VSAnalysisTransformer.ANALYSIS_NAME;
        PackManager.v().getPack("jap").add(
                new Transform(analysisName,
                        VSAnalysisTransformer.getInstance())
        );

    }

    @Test
    public void testVSAnalysis() {
        addTestClass("inputs.Test1");
        Main.main(getArgs());
    }
}
