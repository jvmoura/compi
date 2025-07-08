import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import mips.MipsFrame;
import Symbol.Symbol;
import Tree.PrintTree;
import symboltable.SymbolTable;
import FragAux.Frag;
import FragAux.ProcFrag;
import VisitorIR.BuildIRVisitor;
import syntaxtree.Program;
import visitor.BuildSymbolTableVisitor;
import visitor.TypeCheckVisitor;
import visitor.BuildSymbolTableVisitor;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Compilador MiniJava: Teste de Geração de IR ---");

        // 1. Parse do arquivo de entrada
        String filePath = args.length > 0 ? args[0] : "MiniJavaComp/Teste.java";
        System.out.println("Analisando o arquivo: " + filePath + "\n");
        MiniJava parser = new MiniJava(new FileInputStream(filePath));
        Program prog = parser.Goal();

        // 2. Construção da Tabela de Símbolos
        System.out.println("--- Construindo a Tabela de Símbolos ---");
        SymbolTable st = new SymbolTable();
        prog.accept(new BuildSymbolTableVisitor(st)); // Executa o seu visitor!
        System.out.println("Tabela de Símbolos construída com sucesso.\n");

        // 3. Checagem de Tipos (Opcional, mas recomendado)
        System.out.println("--- Verificando Tipos ---");
        prog.accept(new TypeCheckVisitor(st));
        System.out.println("Verificação de tipos concluída com sucesso.\n");

        // 4. Geração do Código Intermediário (IR)
        System.out.println("--- Gerando Representação Intermediária (IR) ---");
        MipsFrame frame = new MipsFrame(Symbol.symbol("main"), new ArrayList<Boolean>());
        BuildIRVisitor irVisitor = new BuildIRVisitor(frame, st);
        prog.accept(irVisitor);
        System.out.println("Geração de IR completa.\n");

        // 5. Impressão dos "fragments" de IR gerados
        System.out.println("--- Fragments de IR Gerados ---");
        Frag frag = irVisitor.frags.getNext(); // Pula o primeiro fragmento vazio
        PrintTree pt = new PrintTree(System.out);

        while (frag != null) {
            if (frag instanceof ProcFrag) {
                ProcFrag procFrag = (ProcFrag) frag;
                System.out.println("--- Fragmento para o Método: " + procFrag.frame.name + " ---");
                pt.prStm(procFrag.body);
                System.out.println("---------------------------------------\n");
            }
            frag = frag.getNext();
        }
    }
}