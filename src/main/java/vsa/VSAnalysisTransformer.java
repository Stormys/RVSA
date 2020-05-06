package vsa;

import soot.*;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.*;

public class VSAnalysisTransformer extends BodyTransformer {
    public static final String ANALYSIS_NAME = "jap.vsa-analysis";

    private static VSAnalysisTransformer theInstance =
            new VSAnalysisTransformer();

    public static VSAnalysisTransformer getInstance() {
        return theInstance;
    }

    @Override
    protected void internalTransform(Body body, String phaseName, Map<String, 
                                     String> options) {
        VSA analysis = new VSA(new ExceptionalUnitGraph(body));
    }
}
