package Regalloc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import Graph.Node;
import Graph.NodeList;
import Temp.Temp;
import Temp.TempList;
import FlowGraph.AssemFlowGraph;

public class Liveness extends InterferenceGraph {
    /* Relaciona nó a uma lista de Temp:
    *  Relaciona um nó a seu conjunto de temporários ativos
    */
    HashMap<Node, TempList> liveMap;
    AssemFlowGraph flowGraph;
    HashMap<Temp, Node> tnode; // Relaciona Temp a um nó

    public Liveness(AssemFlowGraph fg) {
        flowGraph = fg;

        calculateLiveOut();

        // Constrói o grafo de interferência com base na análise de longevidade

        tnode = new HashMap<Temp, Node>();
        for (NodeList nodes = flowGraph.nodes(); nodes.tail != null; nodes = nodes.tail) {
            
            TempList def = flowGraph.def(nodes.head); // Obtém temporários que são definidos no nó n
            for (TempList d = def; d != null; d = d.tail) {
                if (!tnode.containsKey(d.head)) {
                    // Temporário ainda não está mapeado no hashmap que associa Temp a Node
                    Node node = this.newNode();
                    tnode.put(d.head, node);
                }
            }

            TempList liveOut = liveMap.get(nodes.head); // Obtém o liveOut calculado na análise de longevidade para nó n
            for (TempList lo = liveOut; lo != null; lo = lo.tail) {
                if (!tnode.containsKey(lo.head)) {
                    // Temporário ainda mapeado no hashmap que associa temp a nó
                    Node node = this.newNode();
                    tnode.put(lo.head, node);
                }
            }
        }

        /* Construção do grafo de interferência
         * 1. Em qualquer instrução diferente de MOVE que define uma variável a,
         * onde variáveis em live-out são b1 , ..., bj , adicione arcos de
         * interferência (a, b1 ), ..., (a, bj );
         * 2. Em uma instrução MOVE a ← c, onde variáveis b1 , ..., bj estão no 
         * live-out, adicione arcos de interferência (a, b1 ), ..., (a, bj ) para 
         * qualquer bi que não seja o mesmo que c;
         */

        for (NodeList nodes = flowGraph.nodes(); nodes != null; nodes = nodes.tail) {
            TempList def = flowGraph.def(nodes.head); // Ontém registradores definidos no nó n
            TempList liveOut = liveMap.get(nodes.head); // Live-out do nó n

            for (TempList d = def; d != null; d = d.tail) {
                for (TempList lo = liveOut; lo != null; lo = lo.tail) {
                    // Caso 1
                    if (tnode.get(d.head) != tnode.get(lo.head))
                        this.addEdge(tnode.get(d.head), tnode.get(lo.head));
                }
            }
        }
    }

    // v é live-out no nó n se é ativa em um dos out-edges (arcos de saída) de n;
    public void calculateLiveOut() {
        // useMap[n]: conjunto de variáveis usadas pelo nó n
        HashMap<Node, BitSet> useMap = new HashMap<Node, BitSet>();
        // defMap[n]: conjunto de variáveis definidas pelo nó n
        HashMap<Node, BitSet> defMap = new HashMap<Node, BitSet>();

        // in[n]: define variáveis que são live-in (ativas nos arcos de entrada) de n
        HashMap<Node, BitSet> in = new HashMap<Node, BitSet>();
        // out[n]: define variáveis que são live-out (ativas nos arcos de saída) de n
        HashMap<Node, BitSet> out = new HashMap<Node, BitSet>();

        HashMap<Integer, Temp> numTemp = new HashMap<Integer, Temp>(); // Associa inteiro a um temporário

        NodeList flowNodes = flowGraph.nodes(); // Obtém todas os nós do grafo de fluxo de controle

        for (NodeList fn = flowNodes; fn != null; fn = fn.tail) {
            // Percorre todos os nós do grafo de fluxo de controle para obter seu def e use
            Node nodeIt = fn.head;

            // Inicializa in e out
            in.put(nodeIt, new BitSet());
            out.put(nodeIt, new BitSet());

            TempList def = flowGraph.def(nodeIt); // Obtém os temporários definidos no nó nodeIt
            TempList use = flowGraph.use(nodeIt); // Obtém os temporários usados no nó n
                        
            BitSet defBitSet = new BitSet();
            for (TempList d = def; d != null; d = d.tail) {
                // Percorre todos os temporários definidos no nó n, ajustando o defBitSet
                if (d.head != null) {
                    defBitSet.set(d.head.num); // Coloca bit na posição correspondente ao número do Temp como true
                    numTemp.put(d.head.num, d.head); // Associa número do temporário ao nó onde ele está sendo definido
                }
            }
            defMap.put(nodeIt, defBitSet); // Inicializa o bitset correspondente ao def[nodeIt]

            BitSet useBitSet = new BitSet();
            for (TempList u = use; u != null; u = u.tail) {
                if (u.head != null) {
                    useBitSet.set(u.head.num); // Coloca bit na posição correspondente ao número do Temp como true
                    numTemp.put(u.head.num, u.head); // Associa número do temporário ao nó onde ele está sendo usado
                }
            }
            useMap.put(nodeIt, useBitSet);
        }

        // Implementação do algoritmo 10.4 do livro texto: computação da longevidade por iteração
        boolean allEqual = false; // Indica se in' = in e out' = out (condição de parada do algoritmo)

        while (!allEqual) {
            allEqual = true;

            for (NodeList nl = flowNodes; nl != null; nl = nl.tail) {
                // Percorre todas os nós do grafo de fluxo de controle e calcula in', in e out conforme algoritmo 10.4
                
                Node node = nl.head;

                BitSet in_prime = (BitSet)in.get(node).clone(); // in'[n] <- in[n]
                BitSet out_prime = (BitSet)out.get(node).clone(); // out'[n] <- out[n]

                BitSet newIn = new BitSet(); // Inicializa novo in[n] (será calculado depois)
                newIn.or(useMap.get(node)); // in[n] <- use[n]
                BitSet outDifDef = out.get(node);
                outDifDef.andNot(defMap.get(node)); // out[n] - def[n]
                newIn.or(outDifDef); // in[n] <- use[n] U (out[n] - def[n])
                in.replace(node, newIn); // Armazena resultado no map

                BitSet newOut = new BitSet();
                for (NodeList succsNode = node.succ(); succsNode != null; succsNode = succsNode.tail) {
                    // Percorre sucessores de n para calcular newOut
                    newOut.or(in.get(succsNode.head)); // newOut[n] <- newOut[n] U (s e succ[n]) in[s]
                }
                out.replace(node, newOut); // Armazena resultado no map

                if (!newOut.equals(out_prime) || !newIn.equals(in_prime)) {
                    // Se in'[n] != in[n] OU out'[n] != out[n], repita o processo
                    allEqual = false;
                }
            }
        }

        // Preenchendo o liveMap: contém o conjunto de temporários ativos para cada nó n
        liveMap = new HashMap<Node, TempList>();

        for (NodeList nl = flowNodes; nl != null; nl = nl.tail) {
            Node node = nl.head;

            BitSet liveOut = out.get(node);
            int first = liveOut.nextSetBit(0); // Returns the index of the first bit that is set to true that occurs on or after the specified starting index
            TempList live_out = new TempList(numTemp.get(first), null);

            for (int i = first + 1; i < liveOut.size(); i++) {
                if (liveOut.get(i)) {
                    // Temporário está ativo no nó n: adiciona-o ao live_out
                    Temp t = numTemp.get(i);
                    live_out = new TempList(t, live_out);
                }
            }
            liveMap.put(node, live_out);
        }
    }
}
