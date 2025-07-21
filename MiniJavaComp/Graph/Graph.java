package Graph;

public class Graph {
    NodeList root; // Nó raiz do grafo e seu sucessor
    int nodeCount = 0; // Quantidade de nós do grafo
    NodeList myLast; // Subgrafo que representa último nó adicionado ao grafo

    public Graph() {
        root = null;
    }

    public NodeList nodes() {
        return root; // Retorna nó raiz
    }

    public Node newNode() {
        return new Node(this); // Instancia um novo nó ao grafo
    }

    static boolean inList(Node n, NodeList l) {
        // Verifica se nó n pertence à lista de nós l
        for (NodeList p=l; p!=null; p = p.tail) {
            if (p.head == n) { return true; }
        }
        return false;
    }

    // Operações com arestas

    public void check(Node n) {
        if (n.myGraph != this) {
            // Erro: aresta está sendo adicionada ao grafo g, mas não pertence ao grafo g
            throw new Error("Graph.addEdge using nodes from the wrong graph");
        }
    }

    public void addEdge(Node from, Node to) {
        // Adiciona aresta saindo do nó 'from' e indo para nó 'to'
        check(from);
        check(to);
        if (from.goesTo(to)) {
            // Aresta já existe
            return;
        }

        to.preds = new NodeList(from, to.preds);
        from.succs = new NodeList(to, from.succs);
    }

    public NodeList delete(Node n, NodeList l) {
        if (l == null) {
            // Lista vazia
            throw new Error("Graph.rmEdge: edge nonexistent");
        }

        if (n == l.head) {
            return l.tail; // Remove nó
        }
        return new NodeList(l.head, delete(n, l.tail));
    }

    public void rmEdge(Node from, Node to) {
        // Remove aresta que sai de 'from' e vai para 'to'
        to.preds = delete(from, to.preds);
        from.succs = delete(to, from.succs);
    }

    // Print a human-readable dump for debugging.
    public void show(java.io.PrintStream out) {
        for (NodeList p=nodes(); p!=null; p=p.tail) {
            Node n = p.head;
            out.print(n.toString());
            out.print(": ");
            for(NodeList q=n.succ(); q!=null; q=q.tail) {
                out.print(q.head.toString());
                out.print(" ");
            }
            out.println();
        }
    }
}