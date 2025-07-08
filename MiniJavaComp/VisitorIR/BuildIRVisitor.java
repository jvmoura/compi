package VisitorIR;

import syntaxtree.*;
import symboltable.*;
import symboltable.Class;
import Frame.Access;
import Frame.Frame;
import FragAux.Exp;
import FragAux.*;
import Symbol.Symbol;
import Temp.*;
import Tree.*;
import Tree.ExpList;

import java.util.*;

public class BuildIRVisitor implements VisitorIR.IRVisitor {
    private Frame current_frame;
    public Frag frags;
    public Class current_class;
    public Method current_method;
    public SymbolTable symbolTable;

    public BuildIRVisitor(Frame f, SymbolTable st) {
        this.current_frame = f;
        this.frags = new Frag(null);
        this.symbolTable = st;
    }

    public Exp visit(Program n) {
        n.m.accept(this);

        // Visita cada uma das classes do programa
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.elementAt(i).accept(this);
        }

        return null;
    }

    public Exp visit(MainClass n) {
        String className = n.i1.toString();
        this.current_class = this.symbolTable.getClass(className);
        ArrayList<Boolean> escape_list = new ArrayList<Boolean>();
        escape_list.add(false); // Nenhuma variável escapa: todas ficam nos registradores, nesta fase
        this.current_frame = this.current_frame.newFrame(Symbol.symbol("main"), escape_list);

        Exp exp = n.s.accept(this);  // n.s: comando
        Tree.Expr temp1 = exp.unEx();

        Stm func = new MOVE(new TEMP(this.current_frame.RV()),
                            new ESEQ(new Tree.EXP(temp1), new CONST(1)));
        List<Stm> body = new ArrayList<Stm>();
        body.add(func);

        this.current_frame.procEntryExit1(body); // Implementa mudança de visão

        this.frags.addNext(new ProcFrag(func, this.current_frame));
        
        return null;
    }

    public Exp visit(ClassDeclSimple n) {
        String className = n.i.toString();
        this.current_class = this.symbolTable.getClass(className);

        n.i.accept(this);

        for (int i = 0; i < n.vl.size(); i++) {
            // Aceita declarações das variáveis da classe
            n.vl.elementAt(i).accept(this);
        }

        for (int i = 0; i<n.ml.size(); i++) {
            // Aceita declarações dos métodos da classe
            n.ml.elementAt(i).accept(this);
        }

        return null;
    }

    public Exp visit(ClassDeclExtends n) {
        String className = n.i.toString();
        this.current_class = this.symbolTable.getClass(className);

        n.i.accept(this);
        n.j.accept(this);

        for (int i = 0; i < n.vl.size(); i++) {
            // Aceita declarações das variáveis da classe
            n.vl.elementAt(i).accept(this);
        }

        for (int i = 0; i < n.ml.size(); i++) {
            // Aceita declarações dos métodos da classe
            n.ml.elementAt(i).accept(this);
        }

        return null;
    }

    public Exp visit(VarDecl n) {
        n.i.accept(this);
        n.t.accept(this);

        return null;
    }

    public Exp visit(MethodDecl n) {
        String methName = n.i.toString();
        this.current_method = this.current_class.getMethod(methName);
        List<Boolean> escape_list = new ArrayList<Boolean>();
        
        for (int i = 0; i < n.fl.size(); i++) {
            // Nessa etapa, NENHUM parâmetro escapa (todos ficam nos registradores)
            escape_list.add(false);
        }

        // Frame do método atual
        this.current_frame = this.current_frame.newFrame(Symbol.symbol(current_class.getId() + "$" + current_method.getId()), escape_list);

        for (int i = 0; i < n.fl.size(); i++) {
            // Aceita as declarações dos parâmetros (formais) do método
            n.fl.elementAt(i).accept(this);
        }

        for (int i = 0; i < n.vl.size(); i++) {
            // Aceita as declarações das variáveis do método
            n.vl.elementAt(i).accept(this);
        }

        /************ Tratando os comandos do método *******/
        Stm body = new Tree.EXP(new CONST(0));

        for (int i = 0; i < n.sl.size(); i++) {
            Tree.Expr temp1 = n.sl.elementAt(i).accept(this).unEx();
            body = new SEQ(body, new EXP(temp1));
        }

        /**** Tratando a execução do método *****/
        Tree.Expr ret = new ESEQ(body, n.e.accept(this).unEx()); // Executa corpo do método (comandos) e avalia a expressão o retorno 
        Stm func = new MOVE(new TEMP(this.current_frame.RV()), ret); // Move o valor de retorno (resultado) para o registrador adequando (RV)

        List<Stm> body_list = new ArrayList<Stm>();
        body_list.add(func);

        this.current_frame.procEntryExit1(body_list); // Implementa mudança de visão

        Frag next = this.frags;
        
        while (next.hasNext()) {
            next = next.getNext();
        }
        
        next.addNext(new ProcFrag(func, this.current_frame));

        return null;
    }

    public Exp visit(Formal n) {
        n.i.accept(this);
        n.t.accept(this);

        return null;
    }

    public Exp visit(IntArrayType n) { return null; }

    public Exp visit(BooleanType n) { return null; }

    public Exp visit(IntegerType n) { return null; }

    public Exp visit(IdentifierType n) { return null; }

    public Exp visit(Block n) {
        Tree.Expr temp1 = n.sl.elementAt(0).accept(this).unEx();
        SEQ acc = new SEQ(new EXP(new CONST(0)), new EXP(temp1));

        EXP curr_stm;
        for (int i = 1; i < n.sl.size(); i++) {
            // Adiciona os métodos do bloco sequencialmente
            Tree.Expr temp2 = n.sl.elementAt(i).accept(this).unEx();
            curr_stm = new EXP(temp2);

            acc = new SEQ(acc, curr_stm);
        }

        return new Exp(new ESEQ(acc, new CONST(1)));
    }

    public Exp visit(If n) {
        Exp exp  = n.e.accept(this),
            stm1 = n.s1.accept(this),
            stm2 = n.s2.accept(this);
        
        Label t   = new Label(),
              f   = new Label(),
              end = new Label();
        
        // Verifica se exp é verdadeira ou falsa e desvia para local (label) adequado
        CJUMP cjump = new CJUMP(CJUMP.EQ, exp.unEx(), new CONST(1), t, f);

        // Caso em que exp é verdadeira: executa stm1 e vai para end
        Tree.Expr temp1 = stm1.unEx();
        SEQ true_jump = new SEQ(new EXP(temp1), new JUMP(end));

        // Label t com execução do Stm para quando exp é verdadeira
        SEQ true_ = new SEQ(new LABEL(t), true_jump);

        // Caso em que exp é falsa: executa stm2 e continua o fluxo do programa
        Tree.Expr temp2 = stm2.unEx();
        SEQ false_ = new SEQ(new LABEL(f), new EXP(temp2));

        // Caso verdadeiro ou falso
        SEQ true_or_false = new SEQ(true_, false_);

        // Corpo do if
        SEQ mainSeq = new SEQ(cjump, true_or_false);

        return new Exp(new ESEQ(new SEQ(mainSeq, new LABEL(end)), new CONST(1)));
    }

    public Exp visit(While n) {
        Exp exp = n.e.accept(this),
            stm = n.s.accept(this);

        Label body = new Label(),
              end  = new Label(),
              test = new Label();
        
        // Verifica se exp é V ou F e decide se executa body ou vai sai do laço (vai para end)
        CJUMP cjump = new CJUMP(CJUMP.EQ, exp.unEx(), new CONST(1), body, end); // Permanece no corpo enquanto exp = true
        JUMP jump = new JUMP(test); // Retorna à linha que testa a condição (exp)
        
        SEQ check = new SEQ(new LABEL(test), cjump); // A partir da linha test, executa o cjump
        Tree.Expr temp1 = stm.unEx();
        SEQ exec = new SEQ(new LABEL(body), new EXP(temp1));

        SEQ loop = new SEQ(exec, jump);
        SEQ while_ = new SEQ(check, loop);
        SEQ main = new SEQ(while_, new LABEL(end));

        return new Exp(new ESEQ(main, new CONST(1)));
    }

    public Exp visit(Print n) {
        Exp e = n.e.accept(this);
        
        List<Tree.Expr> args = new LinkedList<Tree.Expr>();
        args.add(e.unEx());

        Tree.Expr exp = this.current_frame.externalCall("print", args);

        return new Exp(exp);
    }

    public Exp visit(Assign n) {
        Tree.Expr i = n.i.accept(this).unEx();
        Tree.Expr e = n.e.accept(this).unEx();

        // Mova para o endereço de memória da variável com identificador i o valor e
        return new Exp(new ESEQ(new MOVE(new MEM(i), e), new CONST(1)));
    }

    public Exp visit(ArrayAssign n) {
        // return new Exp(new ESEQ(move, new CONST(0)));
        Exp array = n.i.accept(this), // Posição da memória onde array inicia
            pos = n.e1.accept(this),  // Indice do array a ser acessado
            val = n.e2.accept(this);  // Valor a ser atribuído à posição referente ao índice do array

        
        Label t = new Label(), f = new Label();

        // Verifica se posição acessada é válida
        Stm indexValid = new CJUMP(CJUMP.LT, pos.unEx(), new MEM(array.unEx()), t, f);

        // Calcula as posições
        BINOP offset_pos = new BINOP(BINOP.PLUS, new CONST(1), pos.unEx()); // Quantidade de saltos a partir da posição inicial
        BINOP offset_bytes = new BINOP(BINOP.MUL, offset_pos, new CONST(this.current_frame.wordSize())); // Quantidade de bytes para obter a posição desejada (saltos * tam. da palavra)
        BINOP elem_pos = new BINOP(BINOP.PLUS, offset_bytes, array.unEx()); // Endereço do elemento cujo valor será atribuído (posicão inicial + desloc. em bytes)

        // Caso a posição seja válida, move o valor (val) para endereço calculado antes
        Stm val_pos = new SEQ(new LABEL(t), new MOVE(new MEM(elem_pos), val.unEx()));

        // Checa se posição é válida e executa de acordo
        Stm res = new SEQ(new SEQ(indexValid, val_pos), new LABEL(f));

        return new Exp(new ESEQ(res, new CONST(1)));
    }

    public Exp visit(And n) {
        Exp e1 = n.e1.accept(this),
            e2 = n.e2.accept(this);

        Label t = new Label();  // Label se e1 é verdadeira
        Label f = new Label();  // Label final
        Label tt = new Label(); // Label se e2 é verdadeira

        Temp rv = new Temp(); // Registrador que armazena o resultado

        Stm init_out = new MOVE(new TEMP(rv), new CONST(0)); // Inicia como false (0)
        Stm ok = new MOVE(new TEMP(rv), new CONST(1)); // Caso ambas as expressões sejam verdadeiras, altera valor para true (1)

        // Checa se e1 é verdadeira
        Stm ex1 = new CJUMP(CJUMP.EQ, e1.unEx(), new CONST(1), t, f);
        // Checa se e2 é verdadeira
        Stm ex2 = new CJUMP(CJUMP.EQ, e2.unEx(), new CONST(1), t, f);

        // Estrutura referente ao &&
        Stm stm = new SEQ(new SEQ(ex1, new SEQ(new LABEL(t), ex2)),
                  new SEQ(new LABEL(tt), new SEQ(ok, new LABEL(f))));

        return new Exp(new ESEQ(stm, new MEM(new TEMP(rv))));
    }

    public Exp visit(LessThan n) {
        Exp e1 = n.e1.accept(this),
            e2 = n.e2.accept(this);

        Label t = new Label();
        Label f = new Label();

        Temp rv = new Temp(); // Registrador que armazena resultado

        // Inicializa resultado como zero (falso)
        Stm init_res = new MOVE(new TEMP(rv), new CONST(0));

        Stm lt = new CJUMP(CJUMP.LT, e1.unEx(), e2.unEx(), t, f);

        // Estrutura referente ao <
        Stm res = new SEQ(new SEQ(new SEQ(init_res, lt), new SEQ(new LABEL(t), new MOVE(new TEMP(rv), new CONST(1)))), new LABEL(f));
        
        return new Exp(new ESEQ(res, new TEMP(rv)));
    }

    public Exp visit(Plus n) {
        Exp e1 = n.e1.accept(this);
        Exp e2 = n.e2.accept(this);
    
        BINOP binop = new BINOP(BINOP.PLUS, e1.unEx(), e2.unEx());
    
        return new Exp(binop);
    }

    public Exp visit(Minus n) {
        Exp e1 = n.e1.accept(this);
        Exp e2 = n.e2.accept(this);
    
        BINOP binop = new BINOP(BINOP.MINUS, e1.unEx(), e2.unEx());
    
        return new Exp(binop);
    }

    public Exp visit(Times n) {
        Exp e1 = n.e1.accept(this);
        Exp e2 = n.e2.accept(this);
    
        BINOP binop = new BINOP(BINOP.MUL, e1.unEx(), e2.unEx());
    
        return new Exp(binop);
    }

    public Exp visit(ArrayLookup n) {
        Exp array = n.e1.accept(this);
        Exp pos = n.e2.accept(this);

        Label valid = new Label();
        Label invalid = new Label();
        Temp rv = new Temp();

        Stm indexValidation = new CJUMP(CJUMP.LT, pos.unEx(), new MEM(array.unEx()), valid, invalid);

        // Calculando endereço do índice do array na memória
        BINOP offset_pos = new BINOP(BINOP.PLUS, new CONST(1), pos.unEx()); // Quantidade de saltos que devem ser dados da posição inicial até chegar no índice a ser acessado
        BINOP offset_bytes = new BINOP(BINOP.MUL, offset_pos, new CONST(this.current_frame.wordSize())); // Quantidade de bytes para obter posição a ser acessada
        BINOP mem_pos = new BINOP(BINOP.PLUS, array.unEx(), offset_bytes); // Posição do valor a ser acessado na memória

        // Caso a posição seja válida, move o valor na posição mem_pos para o registrador de retorno
        Stm valid_pos = new SEQ(new LABEL(valid), new MOVE(new TEMP(rv), new MEM(mem_pos)));

        Stm res = new SEQ(new SEQ(indexValidation, valid_pos), new LABEL(invalid)); // Executa a operação em si
    
        return new Exp(new ESEQ(res, new TEMP(rv)));
    }

    public Exp visit(ArrayLength n) {
        Exp e = n.e.accept(this);

        return new Exp(new MEM(e.unEx()));
    }

    // TODO
    public Exp visit(Call n) {
        String className = null;

        ExpList list_exp = null; // Lista de argumentos
        for (int i = n.el.size() - 1; i >= 0; i--) {
            list_exp = new ExpList(n.el.elementAt(i).accept(this).unEx(), list_exp);
        }

        list_exp = new ExpList(n.e.accept(this).unEx(), list_exp);

        if (n.e instanceof This) {
            className = this.current_class.getId();
        }

        if (n.e instanceof IdentifierExp) {
            List<Variable> params = Collections.list(current_method.getParams()); // Parâmetros do método
            
            // Busca referência entre os parâmetros do método
            for (Variable v : params) {
                if (v.id() == ((IdentifierExp)n.e).toString()) {
                    className = v.type().toString();
                    break;
                }
            }

            if (className == null) {
                /* Se chegar aqui, n.e não foi encontrado entre os parâmetros do método: Busca
                referencia entre as variáveis do método
                */
                if (current_method.getVar(((IdentifierExp)n.e).toString()) != null) {
                    className = current_method.getVar(((IdentifierExp)n.e).toString()).toString();
                }

                if (className == null) {
                    /* Se chegar aqui, n.e não foi encontrado entre as variáveis do método: Busca
                    referencia entre as variáveis da classe
                    */
                    if (current_class.getVar(((IdentifierExp)n.e).toString()) != null) {
                        className = current_class.getVar(((IdentifierExp)n.e).toString()).toString();
                    }
                }
            }
        }

        if (n.e instanceof NewObject) {
            className = ((NewObject)n.e).i.toString();
        }

        if (className != null) {
            return new Exp(new CALL(new NAME(new Label(className + "$" + n.i.toString())), list_exp));
        }

        return new Exp(new CONST(0));
    }

    public Exp visit(IntegerLiteral n) {
        return new Exp(new CONST(n.i));
    }

    public Exp visit(True n) {
        return new Exp(new CONST(1));
    }

    public Exp visit(False n) {
        return new Exp(new CONST(0));
    }

    public Exp visit(IdentifierExp n) {
        Access a = this.current_frame.allocLocal(false);
        
        return new Exp(a.exp(new TEMP(this.current_frame.FP())));
    }

    public Exp visit(This n) {
        // Retorna o "ponteiro" para início do frame atual
        return new Exp(new MEM(new TEMP(this.current_frame.FP())));
    }

    public Exp visit(NewArray n) {
        Exp e = n.e.accept(this); // Representa o tamanho

        BINOP size = new BINOP(BINOP.PLUS, new CONST(1), e.unEx());
        BINOP size_bytes = new BINOP(BINOP.MUL, size, new CONST(this.current_frame.wordSize()));
    
        List<Tree.Expr> args = new LinkedList<Tree.Expr>();
        args.add(size_bytes);

        Tree.Expr return_exp = this.current_frame.externalCall("initArray", args); 
    
        return new Exp(return_exp);
    }

    public Exp visit(NewObject n) {
        String className = n.i.accept(this).toString();
        int nVars = 0;

        symboltable.Class class_ = this.symbolTable.getClass(className);
        if (class_ != null) {
            nVars = class_.getAllVars().size();
        }

        BINOP size = new BINOP(BINOP.PLUS, new CONST(1), new CONST(nVars));
        BINOP size_bytes = new BINOP(BINOP.MUL, size, new CONST(this.current_frame.wordSize()));

        List<Tree.Expr> args = new LinkedList<Tree.Expr>();
        args.add(size_bytes);

        // CALL returnExp = this.current_frame.externalCall("malloc", args);
        Tree.Expr returnExp = this.current_frame.externalCall("malloc", args);

        return new Exp(returnExp);
    }

    public Exp visit(Not n) {
        Exp e = n.e.accept(this);

        BINOP binop = new BINOP(BINOP.XOR, new CONST(1), e.unEx());

        return new Exp(binop);
    }

    public Exp visit(Identifier n) {
        Access a = this.current_frame.allocLocal(false); // Armazena no registrador

        return new Exp(a.exp(new TEMP(this.current_frame.FP())));
    }
}
