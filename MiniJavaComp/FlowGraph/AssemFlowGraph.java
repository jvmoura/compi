package FlowGraph;

import java.util.HashMap;
import java.util.Hashtable;

import Graph.Node;
import Assem.Instr;
import Assem.InstrList;
import Assem.LABEL;
import Temp.Label;
import Temp.LabelList;
import Temp.TempList;

public class AssemFlowGraph extends FlowGraph {
    public Hashtable<Instr, Node> map; // Associa cada instrução a um nó
    public Hashtable<Node, Instr> rmap; // Associa cada nó a uma instrução

    // Funções sugeridas pelo livro-texto
    public AssemFlowGraph(InstrList instrs) {
        // Recebe uma lista de instruções e returna o grafo de fluxo de controle
        /* Para construir grafo de fluxo:
           1. instruções jump presentes em instrs são usadas para criação de arestas de fluxo de controle;
           2. informação sobre use e def são obtidas usando src e dst, respectivamente, nas instruções de instrs
           3. essa informação é anexada ao nó por meio dos métodos use e def do grafo de fluxo
        */

        map = new Hashtable<Instr, Node>();
        rmap = new Hashtable<Node, Instr>();
        buildAssemFlowGraph(instrs);
    }

    public void buildAssemFlowGraph(InstrList instrs) {
        // Associa cada label (nome que representa um endereço de memória) a uma instrução
        HashMap<Label, Instr> labelMap = new HashMap<Label, Instr>();

        // Percorre instruções buscando as que são instância de LABEL (ponto no programa para qual jump pode ir)
        for (InstrList instr = instrs; instr != null; instr = instr.tail) {
            Instr inst = instr.head;
            Node n = this.newNode();
            map.put(inst, n); // Associa instrução ao nó
            rmap.put(n, inst); // Associa nó à instrução

            if (inst instanceof LABEL) {
                labelMap.put(((LABEL)inst).label, inst); // Associa label (endereço da instrução para qual jump pode ir) à instrução
            }
        }

        // Percorre instruções para preencher map e rmap
        for (InstrList instr = instrs; instr != null; instr = instr.tail) {
            Instr inst = instr.head;

            if (instr.tail != null) {
                // Adiciona aresta entre instrução atual (inst) e instrução seguinte
                Instr nextInstr = (instr.tail).head;
                this.addEdge(map.get(inst), map.get(nextInstr));
            }

            if (inst.jumps() != null) {
                // Instrução tem desvios: adiciona instruções referentes a cada um dos labels como próxima instrução da inst. atual (inst)
                LabelList labels = inst.jumps().labels; // Obtém os endereços os labels que representam endereços de memória
                
                this.addEdge(map.get(inst), map.get(labelMap.get(labels.head)));
                while (labels.tail != null) {
                    // Adiciona cada uma das instruções endereçadas pelo label como instrução seguinte à atual
                    labels = labels.tail;
                    this.addEdge(map.get(inst), map.get(labelMap.get(labels.head)));
                }
            }
        }
    }

    // Implementação das funções da classe abstrata FlowGraph
    public TempList def(Node n) {
        // Retorna quais temporários são definidos neste nó (registro de destino da instrução)
        return rmap.get(n).def();
    }

    public TempList use(Node n) {
        // Retorna quais temporários são usados neste nó (registro de origem da instrução)
        return rmap.get(n).use();
    }

    public boolean isMove(Node n) {
        // Verifica se instrução é MOVE (pode ser excluída se def e use são idênticos)
        return false; // (?) verificação necessária
    }
}
