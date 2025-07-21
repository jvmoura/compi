package Regalloc;

import Graph.Graph;
import Graph.Node;

abstract public class InterferenceGraph extends Graph {
    // abstract public Node tnode(Temp temp); // Relaciona um Temp a um nó
    // abstract public Temp gtemp(Node node); // Retorna nó associado ao Temp
    
    // Informa quais instruções MOVE estão associadas ao grafo
    //(esta é uma dica de quais pares alocar no mesmo registrador)
    // abstract public MoveList moves(); // Será útil na etapa de alocação de registradores

    /* The spillCost(n) is an estimate of how many extra instructions
     * would be executed if n were kept in memory instead of in registers;
     * for a naive spiller, it suffices to return 1 for every n
     */
    public int spillCost(Node node) { return 1; }
}