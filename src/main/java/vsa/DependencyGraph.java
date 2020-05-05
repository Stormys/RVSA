package vsa;

import soot.Local;
import soot.ValueBox;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

import org.jgrapht.*;
import org.jgrapht.graph.*;

public class DependencyGraph {
    private UnitGraph local_graph;
    private Unit initialUnit;
    private Local var;

    private class Node {
        Unit unit;
        ArrayList<Local> vs;
        HashSet<Unit> d;
        DirectedPseudograph<Unit, DefaultEdge> graph;

        Node(Unit unit, ArrayList<Local> vs, HashSet<Unit> d,
             DirectedPseudograph<Unit,DefaultEdge> graph) {
            this.unit = unit;
            this.vs = vs;
            this.d = d;
            this.graph = graph;
        }
    }

    DependencyGraph(UnitGraph graph, Local var, Unit initialUnit) {
        local_graph = graph;
        this.initialUnit = initialUnit;
        this.var = var;

        analysis();
    }

    void analysis() {
        Set<Node> worklist = new HashSet<>();

        DirectedPseudograph<Unit, DefaultEdge> initialGraph = 
                new DirectedPseudograph(DefaultEdge.class);
        ArrayList<Local> initial_vs = new ArrayList<Local>();
        initial_vs.add(var);
        HashSet<Unit> initial_d = new HashSet<Unit>();

        Node initalNode = new Node(initialUnit, initial_vs, initial_d,
                                   initialGraph);
        worklist.add(initalNode);
        
        while (worklist.size() != 0) {
            // get next element
            Iterator it = worklist.iterator();
            Node cur_node = (Node) it.next();
            it.remove();

            System.out.println(cur_node.unit);

            ArrayList<Local> vs = cur_node.vs; 
            HashSet<Unit> d = cur_node.d;

            // get define var
            ArrayList<Local> vsd = GetDefineVar(vs, cur_node.unit);
            if (vsd.size() != 0) {
                ArrayList<Local> vsu = GetUseVar(cur_node.unit);

                // copy vs
                vs = new ArrayList<Local>(vs);
                // remove all define variables
                vs.removeAll(vsd); 
                // add the used vars
                vs.addAll(vsu);

                // copy d
                d = new HashSet<Unit>(d);
                // add unit to d
                d.add(cur_node.unit);

                if (vs.size() == 0) {
                    // TODO
                    continue;
                }
            }

            // add previous basic block
            for (Unit unit : local_graph.getPredsOf(cur_node.unit)) {
                // copy graph
                DirectedPseudograph<Unit, DefaultEdge> new_graph = 
                        (DirectedPseudograph<Unit, DefaultEdge>)
                         ((AbstractBaseGraph)cur_node.graph).clone();
                
                // add edges to previous
                new_graph.addVertex(cur_node.unit);
                new_graph.addVertex(unit);
                new_graph.addEdge(cur_node.unit, unit);

                worklist.add(new Node(unit, vs, d, new_graph));
            }
        }
    }

    ArrayList<Local> GetDefineVar(ArrayList<Local> vs, Unit unit) {
        ArrayList<Local> vsd = new ArrayList<Local>();

        for (ValueBox box : unit.getDefBoxes()) {
            Local def_var = (Local) box.getValue();
            
            if (vs.contains(def_var)) {
                vsd.add(def_var);
            }
        }
       return vsd;
    }

    ArrayList<Local> GetUseVar(Unit unit) {
        ArrayList<Local> vsu = new ArrayList<Local>();

        for (ValueBox box : unit.getUseBoxes()) {
            if (box.getValue() instanceof Local) {
                vsu.add((Local) box.getValue());
            }
        }
        return vsu;
    }
}
