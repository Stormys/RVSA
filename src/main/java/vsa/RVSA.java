package vsa;

import soot.Local;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import soot.jimple.Stmt;
import java.util.Arrays;
import java.util.stream.*;
import soot.NormalUnitPrinter;

import soot.jimple.MulExpr;
import soot.jimple.DivExpr;
import soot.jimple.AddExpr;
import soot.jimple.SubExpr;
import soot.jimple.IntConstant;
import soot.jimple.ArrayRef;
import soot.Value;

public class RVSA {
   private class State {
        Set<Unit> mb;
        Sigma input_state;
        Sigma output_state;

        State (Sigma input_state) {
            this.input_state = input_state;
            this.output_state = new Sigma();

            this.mb = new HashSet();
        }
        public String toString() {
            return output_state.toString();           
        }
  }
   
    private UnitGraph graph;
    private DependencyGraph dgraph;
    private Local orig_var; 
    private Map<Unit, Sigma> orig_sigmaAt;
    private Map<Unit, Set<State>> sigmaAt;

    RVSA(UnitGraph graph, Unit unit, Local orig_var, Map<Unit, Sigma> sigmaAt) {
        this.graph = graph;
        this.orig_var = orig_var;
        this.orig_sigmaAt = sigmaAt;

        dgraph = new DependencyGraph(graph, orig_var, unit);
        this.sigmaAt = new HashMap();

        if (dgraph.final_graph != null) {
            analysis();
            print_results();
        }
    }

    void print_results() {
        System.out.println("Refined VSA");
        System.out.println("-----------------------------------------------");
        NormalUnitPrinter printer = new NormalUnitPrinter(graph.getBody());
        for (Unit unit: graph.getBody().getUnits()) {
            if (sigmaAt.containsKey(unit)) {
                Stmt stmt = (Stmt) unit;
                System.out.print(stmt.getClass() + ": ");
                stmt.toString(printer);
                System.out.println(printer.output());
                printer.output().setLength(0);
                System.out.println("\t" + sigmaAt.get(stmt).toString());
            }
        }
    }

    void analysis() {
        Set<Unit> worklist = new HashSet<>();
        Set<Unit> entry_nodes = new HashSet<>();
        get_entry_nodes(entry_nodes);

        // init entry nodes
        for (Unit node : entry_nodes) {
            for (Unit succ : graph.getSuccsOf(node)) {
                if (dgraph.final_graph.containsVertex(succ)) {
                    if (!sigmaAt.containsKey(succ)) {
                        sigmaAt.put(succ, new HashSet<State>());
                    }
                    sigmaAt.get(succ).add(new State(orig_sigmaAt.get(node)));
                    worklist.add(succ);
                }
            }
        }

        while (worklist.size() != 0) {
            // get next element
            Iterator it = worklist.iterator();
            Unit cur_unit = (Unit) it.next();
            it.remove();

            for (State state : sigmaAt.get(cur_unit)) {
                State new_state = do_analysis(cur_unit, state); 
                if (dgraph.mb.contains(cur_unit)) {
                    new_state.mb.add(cur_unit);
                }

                // add successors to worklist
                for (Unit succ : graph.getSuccsOf(cur_unit)) {
                    if(dgraph.final_graph.containsVertex(succ)) {
                        if (!sigmaAt.containsKey(succ)) {
                            sigmaAt.put(succ, new HashSet<State>());
                            sigmaAt.get(succ).add(new State(new_state.input_state));
                            worklist.add(succ);
                        } else {
                            sigmaAt.get(succ).add(new State(new_state.input_state));
                            worklist.add(succ);
                        }
                    }
                }
            }
      
        }
    }

    void get_entry_nodes(Set<Unit> list) {
        for (Unit node : dgraph.final_graph.vertexSet()) {
            if (dgraph.final_graph.inDegreeOf(node) == 0) {
                list.add(node);
            }
        }
    }

    State do_analysis(Unit node, State state) {
        state.input_state.copy(state.output_state);

        if (node instanceof soot.jimple.AssignStmt) {
            soot.jimple.AssignStmt stmt = (soot.jimple.AssignStmt) node;

            if (stmt.getLeftOp() instanceof Local) {
                Local l = (Local) stmt.getLeftOp();

                if (stmt.getRightOp() instanceof IntConstant) {
                    int val = ((IntConstant) stmt.getRightOp()).value;
                    state.output_state.map.put(l, new L(val, val));
                } else if (stmt.getRightOp() instanceof MulExpr) {
                    // caculate abstraction for mulexpr
                    MulExpr rhs = (MulExpr) stmt.getRightOp();

                    L var_1 = extract_sigma(rhs.getOp1(), state.input_state);
                    L var_2 = extract_sigma(rhs.getOp2(), state.input_state);

                    int[] pos_vals = new int[]{var_1.min * var_2.min, 
                                               var_1.max * var_2.max,
                                               var_1.min * var_2.max,
                                               var_1.max * var_2.min};
                    L result = new L(Arrays.stream(pos_vals).min().getAsInt(),
                                   Arrays.stream(pos_vals).min().getAsInt());
                    state.output_state.map.put(l, result);
                } else if (stmt.getRightOp() instanceof AddExpr) {
                    AddExpr rhs = (AddExpr) stmt.getRightOp();

                    L var_1 = extract_sigma(rhs.getOp1(), state.input_state);
                    L var_2 = extract_sigma(rhs.getOp2(), state.input_state);

                    L result = new L(var_1.min + var_2.min, 
                                     var_1.max + var_2.max);
                    state.output_state.map.put(l, result);
                } else if (stmt.getRightOp() instanceof DivExpr) {

                } else if (stmt.getRightOp() instanceof SubExpr) {
                }
            }
        }
        return new State(state.output_state);
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
}
