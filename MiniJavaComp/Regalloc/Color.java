package Regalloc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Iterator;

import Assem.InstrList;
import FlowGraph.AssemFlowGraph;
import Graph.Node;
import Graph.NodeList;
import Temp.Temp;
import Temp.TempMap;
import mips.MipsFrame;
import Frame.Access;

public class Color implements TempMap {

    private Liveness interferenceGraph;
    private MipsFrame frame;

    // --- Estruturas de Dados do Algoritmo ---

    // Número de registradores disponíveis para coloração
    private int K; 
    
    // Pilha com os nós removidos do grafo para posterior coloração
    private Stack<Node> selectStack = new Stack<Node>();

    // Conjuntos de nós (temporários)
    private HashSet<Node> precolored = new HashSet<Node>(); // Registradores da máquina, já coloridos
    private HashSet<Node> initial = new HashSet<Node>();     // Nós temporários que ainda não foram processados
    private HashSet<Node> simplifyWorklist = new HashSet<Node>(); // Lista de nós de baixo grau, prontos para simplificação
    private HashSet<Node> spillWorklist = new HashSet<Node>();   // Lista de nós de alto grau, candidatos a spill
    private HashSet<Node> spilledNodes = new HashSet<Node>();    // Nós que foram selecionados para spill nesta iteração
    private HashSet<Node> coloredNodes = new HashSet<Node>();    // Nós que já receberam uma cor com sucesso

    // Tabela de graus: mapeia um nó ao seu número de vizinhos no grafo
    private Hashtable<Node, Integer> degree = new Hashtable<Node, Integer>();
    
    // Tabela de cores: mapeia cada nó a um registrador (string)
    private Hashtable<Node, String> color = new Hashtable<Node, String>();

    public InstrList instrs; // A lista de instruções final (a ser retornada)

    public Color(Liveness ig, MipsFrame f, InstrList insns) {
        this.interferenceGraph = ig;
        this.frame = f;
        this.instrs = insns;
        
        // K = Número de registradores MIPS disponíveis para uso geral
        K = frame.registers().length; 

        // 1. Inicia as estruturas de dados (Build & MakeWorklist)
        buildAndMakeWorklist();

        // 2. Executa o algoritmo principal
        do {
            if (!simplifyWorklist.isEmpty()) {
                simplify();
            } else if (!spillWorklist.isEmpty()) {
                selectSpill();
            }
        } while (!simplifyWorklist.isEmpty() || !spillWorklist.isEmpty());

        // 3. Atribui as cores (registradores)
        assignColors();

        // 4. Se houver spills, reescreve o programa e tenta novamente
        if (!spilledNodes.isEmpty()) {
           // (Lógica de reescrita virá aqui)
        }
    }

    private void buildAndMakeWorklist() {
        // Itera por todos os nós do grafo de interferência
        for (NodeList nodes = interferenceGraph.nodes(); nodes != null; nodes = nodes.tail) {
            Node n = nodes.head;
            int deg = n.degree();
            degree.put(n, deg); // Armazena o grau inicial do nó

            // Por enquanto, vamos assumir que não há nós pré-coloridos
            // (Isso mudaria se você fizesse otimizações mais complexas)
            
            if (deg >= K) {
                spillWorklist.add(n); // Grau alto -> candidato a spill
            } else {
                simplifyWorklist.add(n); // Grau baixo -> pode ser simplificado
            }
        }
    }

    // (Outros métodos do algoritmo virão aqui)

    public String tempMap(Temp t) {
        // Implementação da interface TempMap (será preenchida no final)
        return null; 
    }

    private void simplify() {
        // Pega um nó da lista de simplificação
        Iterator<Node> it = simplifyWorklist.iterator();
        Node n = it.next();
        it.remove();

        // Empilha para a coloração posterior
        selectStack.push(n);

        // Decrementa o grau de todos os seus vizinhos
        for (NodeList neighbors = n.adj(); neighbors != null; neighbors = neighbors.tail) {
            decrementDegree(neighbors.head);
        }
    }

    private void selectSpill() {
        // Heurística simples: escolhe o primeiro nó da lista de spill
        Iterator<Node> it = spillWorklist.iterator();
        Node spillNode = it.next();
        it.remove();

        // Marca-o para simplificação (agora ele será tratado como nó de baixo grau)
        simplifyWorklist.add(spillNode);
        
        // Decrementa o grau dos seus vizinhos
        for (NodeList neighbors = spillNode.adj(); neighbors != null; neighbors = neighbors.tail) {
            decrementDegree(neighbors.head);
        }
    }

    private void decrementDegree(Node m) {
        int d = degree.get(m);
        degree.put(m, d - 1);

        // Se o grau de um vizinho 'm' caiu de K para K-1,
        // ele pode ser movido da lista de spill para a de simplificação.
        if (d == K) {
            spillWorklist.remove(m);
            simplifyWorklist.add(m);
        }
    }

    private void assignColors() {
        // Array de todos os registradores MIPS disponíveis
        Temp[] availableRegisters = frame.registers();

        while (!selectStack.empty()) {
            Node n = selectStack.pop();
            HashSet<String> okColors = new HashSet<String>();

            // Adiciona todos os registradores MIPS como cores inicialmente disponíveis
            for (Temp reg : availableRegisters) {
                okColors.add(frame.tempMap(reg));
            }

            // Remove as cores que já estão a ser usadas pelos vizinhos
            for (NodeList neighbors = n.adj(); neighbors != null; neighbors = neighbors.tail) {
                Node neighbor = neighbors.head;
                if (color.containsKey(neighbor)) { // Se o vizinho já foi colorido
                    okColors.remove(color.get(neighbor));
                }
            }

            if (okColors.isEmpty()) {
                // Não há cores disponíveis, este nó DEVE ser derramado (spilled)
                spilledNodes.add(n);
            } else {
                // Atribui a primeira cor disponível
                String chosenColor = okColors.iterator().next();
                color.put(n, chosenColor);
                coloredNodes.add(n);
            }
        }
    }
}