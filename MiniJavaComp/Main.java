import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import mips.MipsFrame;
import Symbol.Symbol;
import symboltable.SymbolTable;
import FragAux.Frag;
import VisitorIR.BuildIRVisitor;
import syntaxtree.Program;

public class Main {
    public static void main(String[] args) throws Exception {
        // 1. Parse o arquivo Minijava
        String file = args.length > 0 ? args[0] : "Teste.java";
        MiniJava parser = new MiniJava(new FileInputStream(file));
        Program prog = parser.Goal();

        // 2. Crie a tabela de símbolos (pode ser preenchida por um visitor de declaração, se necessário)
        SymbolTable st = new SymbolTable();
        // Se você tiver um visitor para preencher a tabela de símbolos, rode aqui
        // prog.accept(new BuildSymbolTableVisitor(st));

        // 3. Crie o frame inicial
        MipsFrame frame = new MipsFrame(Symbol.symbol("main"), new ArrayList<Boolean>());

        // 4. Rode o visitor de IR
        BuildIRVisitor irVisitor = new BuildIRVisitor(frame, st);
        prog.accept(irVisitor);

        // 5. Imprima os fragments IR gerados
        Frag frag = irVisitor.frags;
        while (frag != null) {
            System.out.println(frag);
            frag = frag.getNext();
        }
    }
}