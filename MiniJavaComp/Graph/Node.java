package Graph;

// Representa um nó do grafo
public class Node {
    Graph myGraph; // Grafo ao qual nó pertence
    int myKey; // Chave do nó (contador)
    NodeList succs; // Nós sucessores
    NodeList preds; // Nós predecessores

    public Node(Graph g) {
        myGraph = g;
        myKey = g.nodeCount++; 

        NodeList subGraph = new NodeList(this, null); // Subgrafo a ser adicionado
        if (g.myLast == null) {
            // Grafo está vazio: adiciona nó raiz
            g.root = subGraph;
            g.myLast = subGraph;
        } else {
            // Grafo tem pelo menos um nó: adiciona à cauda do último nó
            g.myLast.tail = subGraph;
            g.myLast = subGraph;
        }
    }


    
    public NodeList succ() { return succs; }

    public NodeList pred() { return preds; }

    NodeList cat(NodeList a, NodeList b) {
        if (a == null) {
            return b;
        } else {
            return new NodeList(a.head, cat(a.tail, b));
        }
    }

    public NodeList adj() { // Retorna lista contendo nós vizinhos
        return cat(succ(), pred());
    }

    int len(NodeList l) {
        int i=0;
        for (NodeList p=l; p!= null; p=p.tail) i++;
        return i;
    }

    public int outDegree() { // Retorna quantos são as arestasde saída
        return len(pred());
    }

    public int inDegree() { // Retorna quantos são as arestas de entrada
        return len(succ());
    }

    public int degree() { // Retorna quantidade de arestas que chegam e saem do nó
        return inDegree() + outDegree();
    }

    public boolean goesTo(Node n) {
        // Verifica se há aresta saindo do nó instanciado para o nó n
        return Graph.inList(n, succ());
    }

    public boolean comesFrom(Node n) { // Verifica se há aresta saindo do nó para o nó instanciado
        return Graph.inList(n, pred());
    }

    public boolean adj(Node n) { // Verifica se nó n pertence à vizinhança do nó nó instanciado
        return goesTo(n) || comesFrom(n);
    }

    public String toString() {
        // Retorna uma srtring com chave do nó
        return String.valueOf(myKey);
    }
}