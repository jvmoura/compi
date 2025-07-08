
package visitor;

import syntaxtree.And;
import syntaxtree.ArrayAssign;
import syntaxtree.ArrayLength;
import syntaxtree.ArrayLookup;
import syntaxtree.Assign;
import syntaxtree.Block;
import syntaxtree.BooleanType;
import syntaxtree.Call;
import syntaxtree.ClassDeclExtends;
import syntaxtree.ClassDeclSimple;
import syntaxtree.False;
import syntaxtree.Formal;
import syntaxtree.Identifier;
import syntaxtree.IdentifierExp;
import syntaxtree.IdentifierType;
import syntaxtree.If;
import syntaxtree.IntArrayType;
import syntaxtree.IntegerLiteral;
import syntaxtree.IntegerType;
import syntaxtree.LessThan;
import syntaxtree.MainClass;
import syntaxtree.MethodDecl;
import syntaxtree.Minus;
import syntaxtree.NewArray;
import syntaxtree.NewObject;
import syntaxtree.Not;
import syntaxtree.Plus;
import syntaxtree.Print;
import syntaxtree.Program;
import syntaxtree.This;
import syntaxtree.Times;
import syntaxtree.True;
import syntaxtree.Type;
import syntaxtree.VarDecl;
import syntaxtree.While;
import symboltable.Class;
import symboltable.Method;
import symboltable.SymbolTable;

public class BuildSymbolTableVisitor implements IVisitor<Void> {
    private SymbolTable symbolTable;
	private Class currClass;
	private Method currMethod;

    public BuildSymbolTableVisitor() {
        symbolTable = new SymbolTable();
        this.currClass = null;
        this.currMethod = null;
    }

    public SymbolTable getSymbolTable(){
        return this.symbolTable;
    }

    // Produção inicial: Program é lista de classes
    public Void visit(Program n) {
        /*
            Program tem dois atributos:
            1) MainClass m      -> classe Main
            2) ClassDeclList cl -> Lista com todas as outras classes do programa
        */
        n.m.accept(this);
        for (int i=0; i<n.cl.size(); i++) {
            // Percorre a lista de classes do programa (cl) e aceita cada uma delas
            n.cl.elementAt(i).accept(this);
        }

        return null;
    }

    // MainClass
    public Void visit(MainClass n) {
        /*
            MainClass tem três atributos:
            1) Identifier i1 -> Identificador (nome) da classe
            2) Identifier i2 -> Identificador (nome) do argumento da função main
            3) Statemtent s  -> Comando a ser executado pela função main
         */
        String className = n.i1.toString(); // Nome da classe
        this.symbolTable.addClass(className, null); // Adiciona classe com id = className na tabela de símbolo (parent é null)
        this.currClass = this.symbolTable.getClass(className);
        this.currClass.addMethod("main", null); // Adiciona método main à classe atual
        this.currMethod = this.currClass.getMethod("main");
        String paramName = n.i2.toString();
        this.currMethod.addParam(paramName, new IntArrayType()); // Adiciona o parâmetro da função main
        n.i1.accept(this);
        n.i2.accept(this);
        n.s.accept(this);
        this.currClass = null;
        this.currMethod = null;
        return null;
    }

    public Void visit(ClassDeclSimple n) {
        /*
            ClassDeclSimple tem três atributos:
            1) Identifier i      -> Identificador (nome) da classe
            2) VarDeclList vl    -> Lista contendo os atributos da classe
            3) MethodDeclList ml -> Lista contendo os métodos da classe
        */
        String className = n.i.toString();
        
        if (!symbolTable.addClass(className, null)) {
            // Erro ao adicionar classe à tabela de síbolos: classe já declarada
            System.err.println("Error" + getFullScope() + ": class " + className + " already exists");
            System.exit(0);
        }

        /* Se chegar aqui, então classe foi adicionada à tabela de síbolos */
        this.currClass = symbolTable.getClass(className);
        n.i.accept(this);
        for (int i=0; i<n.vl.size(); i++) {
            // Aceita cada uma das declarações de variáveis que estão na lista vl
            n.vl.elementAt(i).accept(this);
        }
        for (int i=0; i<n.ml.size(); i++) {
            // Aceita cada uma das declarações de métodos que estão na lista ml
            n.ml.elementAt(i).accept(this);
        }
        this.currClass = null;
        return null;
    }

    public Void visit(ClassDeclExtends n) {
        /* ClassDeclExtends tem 4 atributos:
           1) Identifier i      -> Nome da classe filha
           2) Identifier j      -> Nome da super classe
           3) VarDeclList vl    -> Variáveis globais
           4) MethodDeclList ml -> Lista de métodos da classe
        */
        String className = n.i.toString();
        if (!symbolTable.addClass(className, n.j.toString())) {
            // Erro ao adicionar classe à tabela de síbolos: classe já declarada
            System.err.println("Error" + getFullScope() + ": class " + className + " already exists");
            System.exit(0);
        }

        /* Se chegar aqui, então classe n foi adicionada à tabela de símbolos */
        this.currClass = this.symbolTable.getClass(className);
        n.i.accept(this);
        n.j.accept(this);
        for (int i=0; i<n.vl.size(); i++) {
            // Aceita cada uma das declarações de variáveis que estão na lista vl
            n.vl.elementAt(i).accept(this);
        }        
        for (int i=0; i<n.ml.size(); i++) {
            // Aceita cada uma das declarações de métodos que estão na lista ml
            n.ml.elementAt(i).accept(this);
        }
        this.currClass = null;
        return null;
    }

    public Void visit(VarDecl n) {
        /* VarDecl tem dois atributos:
           1) Type t       -> Tipo da variável
           2) Identifier i -> Identificador (nome) da variável
        */
        String varName = n.i.toString();
        Type varType = n.t;
        if (this.currMethod != null) {
            // Escopo da variável é o método atual: adiciona-a às variáveis do método
            if (!this.currMethod.addVar(varName, varType)) {
                // Erro ao adicionar a variável ao método atual: ela já está definida
                System.err.println("Error" + getFullScope() + ": variable " + varName + " is already defined in method " + currMethod.getId());
                System.exit(0);
            }
        } else {
            // Escopo da variável é a classe (ela é um atributo da classe)
            if (!this.currClass.addVar(varName, varType)) {
                // Erro ao adicionar variável à classe atual: ela á está definida
                System.err.println("Error" + getFullScope() + ": variable " + varName + " is already defined in class " + currClass.getId());
                System.exit(0);
            }
        }
        n.t.accept(this);
        n.i.accept(this);
        return null;
    }

    public Void visit(MethodDecl n) {
        /* MethodDcl tem seis atributos:
           1) Type t;           -> Tipo de retorno
           2) Identifier i;     -> Nome do método
           3) FormalList fl;    -> Parâmetros do método
           4) VarDeclList vl;   -> Variáveis locais
           5) StatementList sl; -> Lista de comandos
           6) Exp e;            -> Expressão de retorno
        */
        String methName = n.i.toString();
        Type returnType = n.t;
        if (!this.currClass.addMethod(methName, returnType)) {
            // Erro ao adicionar método à classe atual: já definido nela
            System.err.println("Error" + getFullScope() + ": method " + methName + "(...) is already defined in class " + currClass.getId());
            System.exit(0);
        }
        /* Se chegar aqui, método foi adicionado na tabela de síbolos, vinculado à classe atual */
		this.currMethod = this.currClass.getMethod(methName);
		n.t.accept(this);
		n.i.accept(this);
		for (int i = 0; i<n.fl.size(); i++) {
            // Aceita cada um dos parâmetros que estão na lista fl (formal list)
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i<n.vl.size(); i++) {
            // Aceita cada uma das variáveis locais do método que estão na lista vl
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i<n.sl.size(); i++) {
            // Aceita cada um dos comandos do método que estão na lista sl
			n.sl.elementAt(i).accept(this);
		}
		n.e.accept(this);
		this.currMethod = null;
        return null;
    }

    public Void visit(Formal n) {
        /* Formal tem dois atributos:
           1) Type t       -> Tipo do parâmetro
           2) Identifier i -> Identificador (nome) do parâmetro
        */
        String paramName = n.i.toString();
        Type paramType = n.t;
        if(!this.currMethod.addParam(paramName, paramType)) {
            // Erro ao adicionar parâmetro à lista de parâmetros do método: ele já existe
			System.err.println("Error" + getFullScope() + ": parameter " + paramName + " is already defined in method " + this.currMethod.getId());
			System.exit(0);
		}
        n.t.accept(this);
		n.i.accept(this);
		return null;
    }

    public Void visit(IntArrayType n) { return null; }

    public Void visit(BooleanType n) { return null; }

    public Void visit(IntegerType n) { return null; }

    public Void visit(IdentifierType n) { return null; }

    public Void visit(Block n) {
        /* Block tem um atributo:
           1) StatementList sl -> lista de comandos
        */
		for (int i = 0; i<n.sl.size(); i++) {
            // Aceita cada um dos comandos da lista de comandos (sl)
			n.sl.elementAt(i).accept(this);
		}
		return null;
    }

    public Void visit(If n) {
        /* If tem três atributos:
           1) Exp e        -> Expressão a ser testada
           2) Statement s1 -> Comando ser executado caso teste retorne verdadeiro
           3) Statement s2 -> Comando a ser executado caso teste retorne falso
         */
        n.e.accept(this);
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
    }

    public Void visit(While n) {
        /* While tem dois atributos:
           1) Exp e       -> Expressão a ser testada
           2) Statement s -> Comando a ser executado
         */
		n.e.accept(this);
		n.s.accept(this);
        return null;
    }

	public Void visit(Print n) {
        /* Print tem um argumento:
           1) Exp e -> Expressão a ser printada
        */
        n.e.accept(this);
        return null;
    }

	public Void visit(Assign n) {
        /* Assign tem dois argumentos:
           1) Identififier i -> Identificador (nome) da variável
           2) Exp e          -> Valor a ser atribuído        
        */
		n.i.accept(this);
		n.e.accept(this);
		return null;
    }

	public Void visit(ArrayAssign n) {
        /* ArrayAssign tem três argumentos:
           1) Identififier i -> Identificador (nome) do array
           2) Exp e1         -> Indice do array
           3) Exp e2         -> Valor a ser atribuído    
        */
		n.i.accept(this);
		n.e1.accept(this);
		n.e2.accept(this);
        return null;
    }

    public Void visit(And n) {
        /* And tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		n.e1.accept(this);
		n.e2.accept(this);
        return null;
    }

	public Void visit(LessThan n) {
        /* LessThan tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		n.e1.accept(this);
		n.e2.accept(this);
        return null;
    }

	public Void visit(Plus n) {
        /* Plus tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		n.e1.accept(this);
		n.e2.accept(this);    
        return null;
    }

	public Void visit(Minus n) {
        /* Minus tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		n.e1.accept(this);
		n.e2.accept(this);   
        return null;
    }

	public Void visit(Times n) {
        /* Times tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		n.e1.accept(this);
		n.e2.accept(this);    
        return null;
    }

	public Void visit(ArrayLookup n) {
        /* ArrayLookup tem dois argumentos:
           1) Exp e1 -> Nome do array
           2) Exp e2 -> Indice do array  
        */
		n.e1.accept(this);
		n.e2.accept(this);
        return null;
    }

	public Void visit(ArrayLength n) {
        /* ArrayLength tem um argumento:
           1) Exp e -> Nome do array
        */
		n.e.accept(this);
        return null;
    }

	public Void visit(Call n) {
        /* Call tem três argumentos:
           1) Exp e        -> Nome da classe
           2) Identifier i -> Nome do método
           3) ExpList el   -> Parâmetros do método
        */
        n.e.accept(this);
		n.i.accept(this);
		for (int i = 0; i < n.el.size(); i++) {
            // Aceita cada um dos parâmetros do método que estão na lista el
			n.el.elementAt(i).accept(this);
		}
        return null;
    }

	public Void visit(IntegerLiteral n) { return null; }

	public Void visit(True n) { return null; }

	public Void visit(False n) { return null; }

	public Void visit(IdentifierExp n) { return null; }

	public Void visit(This n) { return null; }

	public Void visit(NewArray n) {
        /* NewArray tem um atributo:
           1) Expression e -> Tamanho do array
        */
        n.e.accept(this);
        return null;
    }

	public Void visit(NewObject n) {
        /* NewObject tem um atributo:
           1) Indentifier i -> Identificador (nome) do objeto
        */
        return null;
    }

	public Void visit(Not n) {
        /* Not tem um atributo:
           1) Expression e -> Expressão a ser negada
        */
        n.e.accept(this);
        return null;
    }

	public Void visit(Identifier n) { return null; }

    // Função que retorna o escopo completo atual (classe e método)
    public String getFullScope() {
        String scope = "";

        if (currClass != null) {
            scope = currClass.getId();
        }
        if (currMethod != null) {
            scope += "$" + currMethod.getId();
        }

        if (!scope.equals("")) {
            scope = " (" + scope + ")";
        }
        return scope;
    }
}
