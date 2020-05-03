package vsa;

import soot.Local;
import soot.Unit;
import soot.ValueBox;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.*;

import soot.jimple.MulExpr;
import soot.jimple.DivExpr;
import soot.jimple.AddExpr;
import soot.jimple.SubExpr;
import soot.jimple.IntConstant;
import soot.Value;


public class VSA extends ForwardFlowAnalysis {
    Map<Unit, Sigma> sigmaAt;

    private Set<Local> locals = new HashSet<>();

    private Context ctx;

    VSA(UnitGraph graph) {
        this(graph, null, null);
    }

    VSA(UnitGraph graph, Context ctx, Sigma sigma_i) {
        super(graph);
        this.ctx = ctx;
        sigmaAt = new HashMap<>(graph.size() * 2 + 1);

        // Collect locals
        DominatorsFinder<Unit> df = new MHGDominatorsFinder<>(graph);
        for (Unit s: graph) {
            for (Object d : df.getDominators(s)) {
                Unit dominator = (Unit) d;
                for (ValueBox box : dominator.getDefBoxes()) {
                    locals.add((Local) box.getValue());
                }
            }
        }

        for (Unit s : graph) {
            Sigma sigma = new Sigma(locals, state.Bot);
            sigmaAt.put(s, sigma);
        }
        
        doAnalysis();
    }

    private L extract_sigma(soot.Value op, Sigma sigma) {
        if (op instanceof IntConstant) {
            int val = ((IntConstant) op).value;
            return new L(val, val);
        } else if (op instanceof Local) {
            return sigma.map.get((Local) op);
        }
        return new L(state.Top);
    }

    @Override
    protected void flowThrough(Object inValue, Object unit, Object outValue) {
        Sigma sigma_1 = (Sigma) inValue;
        Sigma sigma_2 = (Sigma) outValue;

        sigma_1.copy(sigma_2);

        if (unit instanceof soot.jimple.AssignStmt) {
            soot.jimple.AssignStmt stmt = (soot.jimple.AssignStmt) unit;

            if (stmt.getLeftOp() instanceof Local) {
                Local l = (Local) stmt.getLeftOp();

                if (stmt.getRightOp() instanceof IntConstant) {
                    int val = ((IntConstant) stmt.getRightOp()).value;
                    sigma_2.map.put(l, new L(val, val));
                } else if (stmt.getRightOp() instanceof MulExpr) {
                    // caculate abstraction for mulexpr
                    MulExpr rhs = (MulExpr) stmt.getRightOp();

                    L var_1 = extract_sigma(rhs.getOp1(), sigma_1);
                    L var_2 = extract_sigma(rhs.getOp2(), sigma_1);

                    int[] pos_vals = new int[]{var_1.min * var_2.min, 
                                               var_1.max * var_2.max,
                                               var_1.min * var_2.max,
                                               var_1.max * var_2.min};
                    L result = new L(Arrays.stream(pos_vals).min().getAsInt(),
                                   Arrays.stream(pos_vals).min().getAsInt());
                    sigma_2.map.put(l, result);
                } else if (stmt.getRightOp() instanceof AddExpr) {
                    AddExpr rhs = (AddExpr) stmt.getRightOp();

                    L var_1 = extract_sigma(rhs.getOp1(), sigma_1);
                    L var_2 = extract_sigma(rhs.getOp2(), sigma_1);

                    L result = new L(var_1.min + var_2.min, 
                                     var_1.max + var_2.max);
                    sigma_2.map.put(l, result);
                } else if (stmt.getRightOp() instanceof DivExpr) {

                } else if (stmt.getRightOp() instanceof SubExpr) {
                }
            }
        }
        sigmaAt.put((Unit) unit, sigma_2);
    }

    @Override
    protected Object newInitialFlow() {
        return new Sigma(locals, state.Bot);
    }

    @Override
    protected Object entryInitialFlow() {
        return new Sigma(locals, state.Top);
    }
    
    @Override 
    protected void merge(Object in1, Object in2, Object out) {
        Sigma sigma_1 = (Sigma) in1;
        Sigma sigma_2 = (Sigma) in2;
        Sigma sigma_out = (Sigma) out;

        for (Local local: sigma_1.map.keySet()) {
            L val1 = sigma_1.map.get(local);
            L val2 = sigma_2.map.get(local);

            sigma_out.map.put(local, new L(Math.min(val1.min, val2.min), 
                                           Math.max(val1.max, val2.max)));
        }
    }

    @Override
    protected void copy(Object source, Object dest) {
        Sigma sigma_1 = (Sigma) source;
        Sigma sigma_2 = (Sigma) dest;

        sigma_1.copy(sigma_2);
    }

}
