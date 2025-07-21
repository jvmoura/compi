package FlowGraph;

import Temp.TempList;
import Graph.Node;
import Graph.NodeList;

public abstract class FlowGraph extends Graph.Graph {
    /* Cada nó do grafo de fluxo representa uma instrução
       Grafo de fluxo representa o fluxo de instruções do programa
       x -> y: instrução y pode ser executada após a instrução x
    */

    public abstract TempList def(Node n); // Define quais temporários são definidos neste nó (registro de destino da instrução)
    public abstract TempList use(Node n); // Define quais temporários são usados neste nó (registro de origem da instrução)
    public abstract boolean isMove(Node n); // Define se instrução é MOVE (pode ser excluída se def e use são idênticos)

    // Print a human-readable dump for debugging.
    public void show(java.io.PrintStream out) {
        for (NodeList p=nodes(); p!=null; p=p.tail) {
            Node n = p.head;
            out.print(n.toString());
            out.print(": ");
            for (TempList q=def(n); q!=null; q=q.tail) {
                out.print(q.head.toString());
                out.print(" ");
            }
            
            for (TempList q=use(n); q!=null; q=q.tail) {
                if (q.head != null)
                    out.print(q.head.toString());
                    out.print(" ");
            }
            out.print("; goto ");
            for (NodeList q=n.succ(); q!=null; q=q.tail) {
                out.print(q.head.toString());
                out.print(" ");
            }
            out.println();
        }	
    }
}