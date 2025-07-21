/*******************************************************
 PERCORRE A TABELA DE SIMBOLOS FAZENDO ANALISE SEMANTICA
********************************************************/

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
import symboltable.Variable;

public class TypeCheckVisitor implements IVisitor<Type> {
    private SymbolTable symbolTable;
	private Class currClass;
	private Method currMethod;
    private boolean isMethod;
    private boolean isVariable;

    public TypeCheckVisitor(SymbolTable st) {
        this.symbolTable = st;
        this.currClass = null;
        this.currMethod = null;
        this.isMethod = false;
        this.isVariable = false;
    }

    // Produção inicial: Program é lista de classes
    public Type visit(Program n) {
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
    public Type visit(MainClass n) {
        /*
            MainClass tem três atributos:
            1) Identifier i1 -> Identificador (nome) da classe
            2) Identifier i2 -> Identificador (nome) do argumento da função main
            3) Statemtent s  -> Comando a ser executado pela função main
         */
        String className = n.i1.toString(); // Nome da classe
        this.currClass = this.symbolTable.getClass(className);
        // Obtém o método 'main' associado à classe atual
        this.currMethod = this.symbolTable.getMethod("main", this.currClass.getId());
        n.i1.accept(this);
        this.isVariable = true;
        n.i2.accept(this);
        this.isVariable = false;
        n.s.accept(this);
        this.currClass = null;
        this.currMethod = null;
        return null;
    }

    public Type visit(ClassDeclSimple n) {
        /*
            ClassDeclSimple tem três atributos:
            1) Identifier i      -> Identificador (nome) da classe
            2) VarDeclList vl    -> Lista contendo os atributos da classe
            3) MethodDeclList ml -> Lista contendo os métodos da classe
        */
        String className = n.i.toString();
        this.currClass = this.symbolTable.getClass(className);
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

    public Type visit(ClassDeclExtends n) {
        /* ClassDeclExtends tem 4 atributos:
           1) Identifier i      -> Nome da classe filha
           2) Identifier j      -> Nome da super classe
           3) VarDeclList vl    -> Variáveis globais
           4) MethodDeclList ml -> Lista de métodos da classe
        */
        String className = n.i.toString();
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

    public Type visit(VarDecl n) {
        /* VarDecl tem dois atributos:
           1) Type t       -> Tipo da variável
           2) Identifier i -> Identificador (nome) da variável
        */
        Type varType = n.t.accept(this);
        this.isVariable = true;
        n.i.accept(this);
        this.isVariable = false;
        return varType;
    }

    public Type visit(MethodDecl n) {
        /* MethodDcl tem seis atributos:
           1) Type t;           -> Tipo de retorno
           2) Identifier i;     -> Nome do método
           3) FormalList fl;    -> Parâmetros do método
           4) VarDeclList vl;   -> Variáveis locais
           5) StatementList sl; -> Lista de comandos
           6) Exp e;            -> Expressão de retorno
        */
        String methName = n.i.toString();
        Type returnType = n.t.accept(this);

        // Obtém a declaração do método n na tabela de símbolos associado à classe atual
		this.currMethod = this.symbolTable.getMethod(methName, this.currClass.getId());
        this.isMethod = true;
		n.i.accept(this);
        this.isMethod = false;

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
		this.currMethod = null;
        return returnType;
    }

    public Type visit(Formal n) {
        /* Formal tem dois atributos:
           1) Type t       -> Tipo do parâmetro
           2) Identifier i -> Identificador (nome) do parâmetro
        */
        Type paramType = n.t.accept(this);
        this.isVariable = true;
		n.i.accept(this);
        this.isVariable = false;
		return paramType;
    }

    public Type visit(IntArrayType n) { return n; }

    public Type visit(BooleanType n) { return n; }

    public Type visit(IntegerType n) { return n; }

    public Type visit(IdentifierType n) {
        /* Caso em que uma classe é usada como tipo:
           Verifica se o atributo n.s (que seria o id da classe) está na tabela de símbolo
        */
        if (!this.symbolTable.containsClass(n.s)) {
            // Classe não foi encontrada na tabela de símbolos, logo não pode ser usada como tipo
            System.err.println("Error" + getFullScope() + ": cannot find symbol " + n.s);
            System.exit(0);
        }
        return n;
    }

    public Type visit(Block n) {
        /* Block tem um atributo:
           1) StatementList sl -> lista de comandos
        */
		for (int i = 0; i<n.sl.size(); i++) {
            // Aceita cada um dos comandos da lista de comandos (sl)
			n.sl.elementAt(i).accept(this);
		}
		return null;
    }

    public Type visit(If n) {
        /* If tem três atributos:
           1) Exp e        -> Expressão a ser testada
           2) Statement s1 -> Comando ser executado caso teste retorne verdadeiro
           3) Statement s2 -> Comando a ser executado caso teste retorne falso
        */
        Type t = n.e.accept(this); // Tipo da expressão a ser avaliada no if
        if (!this.symbolTable.compareTypes(t, new BooleanType())) {
            // A expressão inserida no if não é do tipo booleana: informa erro
            System.err.println("Error" + getFullScope() + ": incompatible types: " + this.getTypeName(t) + " cannot be converted to BooloeanType");
            System.exit(0);
        }
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
    }

    public Type visit(While n) {
        /* While tem dois atributos:
           1) Exp e       -> Expressão a ser testada
           2) Statement s -> Comando a ser executado
        */
        Type t = n.e.accept(this); // Tipo da expressão a ser avaliada no while
        if (!this.symbolTable.compareTypes(t, new BooleanType())) {
            // A expressão inserida no while não é do tipo booleana: informa erro
            System.err.println("Error" + getFullScope() + ": incompatible types: " + this.getTypeName(t) + " cannot be converted to BooleanType");
            System.exit(0);
        }
		n.s.accept(this);
		return null;
    }

	public Type visit(Print n) {
        /* Print tem um argumento:
           1) Exp e -> Expressão a ser printada
        */
        n.e.accept(this);
        return null;
    }

	public Type visit(Assign n) {
        /* Assign tem dois argumentos:
           1) Identififier i -> Identificador (nome) da variável
           2) Exp e          -> Valor a ser atribuído        
        */
        this.isVariable = true;
        Type leftType = n.i.accept(this); // Tipo da variável
        this.isVariable = false;

        String varName = n.i.toString();
        boolean varDeclared = false; // true se variável estiver declarada no escopo atual
        // // Verifica se variável está no escopo atual
        if (currMethod != null) {
            // Escopo é o método atual, procura entre suas variáveis locais
            if (currMethod.containsParam(varName)) {
                // Variável aparece como um parâmetro do método atual
                varDeclared = true;
            } else if (currMethod.containsVar(varName)) {
                // Variável aparece como uma variável local do método
                varDeclared = true;
            }
        }
        
        if (currClass != null && !varDeclared) {
            /* Variável não está definida no método: verifica se está definida na classe */
            // Escopo atual é uma classe: busca nome da variável entre os atributos da classe atual e das super classes, se houver
            Class c = currClass;
            while (c != null) {
                if (c.getVar(varName) != null) {
                    // Variável definida no escopo da classe: sai do laço e altera flag
                    varDeclared = true;
                    break;
                } else {
                    // Variável não definida na classe c, verifica na super classe, se houver
                    if (c.parent() == null) {
                        c = null; // Para sair do laço while
                    } else {
                        // Atualiza c para receber o nome da sua super classe imediata (classe mãe)
                        c = this.symbolTable.getClass(c.parent());
                    }
                }
            }
            if (!varDeclared) {
                // Variável não está definida nem no método nem no corpo da classe super classes: informa erro
                System.err.println("Error" + getFullScope() + ": variable " + varName + " is not defined in the " + currClass.getId() + " class or in any of its super classes");
                System.exit(0);
            }
        }

        /* Variável está definida: verifica se o tipo do valor a ser atribuído é compatível com tipo esperado */
        Type rightType = n.e.accept(this);
        if (!this.symbolTable.compareTypes(leftType, rightType)) {
            // Tipo do valor de atribuição é diferente do tipo da variável: informa erro
            System.err.println("Error" + getFullScope() + ": incompatible types: " + this.getTypeName(rightType) + " cannot be converted to " + this.getTypeName(leftType));
            System.exit(0);
        }
		return null;
    }

	public Type visit(ArrayAssign n) {
        /* ArrayAssign tem três argumentos:
           1) Identififier i -> Identificador (nome) do array
           2) Exp e1         -> Indice do array
           3) Exp e2         -> Valor a ser atribuído    
        */
        this.isVariable = true;
        Type lefType = n.i.accept(this); // Tipo da variável (array)
        this.isVariable = false;
        Type indexType = n.e1.accept(this); // Tipo da variável que representa o índice do array
        Type rightType = n.e2.accept(this); // Tipo do valor a ser atribuído à posição do array

        // Verifica se leftType é do tipo IntArrayType
        if (!this.symbolTable.compareTypes(lefType, new IntArrayType())) {
            // Variável que representa array não tem tipo IntArrayType: informa erro
            System.err.println("Error" + getFullScope() + ": IntArrayType required, but " + this.getTypeName(lefType) + " found");
            System.exit(0);
        }
        /* Se chegar aqui, leftType está corretamente tipado, verifica indexType */
        if (!this.symbolTable.compareTypes(indexType, new IntegerType())) {
            // Variável que representa o índice não é IntegerType: informa erro
            System.err.println("Error" + getFullScope() + ": incompatible types: " + this.getTypeName(indexType) + " cannot be converted to IntegerType");
            System.exit(0);
        }
        /* Se chegar aqui, leftType e indexType estão corretamente tipacos, verifica o rightType */
        if (!this.symbolTable.compareTypes(rightType, new IntegerType())) {
            // Valor a ser atribuído à posição do array não é IntegerType (erro, pois apenas array de inteiros é suportado)
            System.err.println("Error" + getFullScope() + ": incompatible types: " + this.getTypeName(rightType) + " cannot be converted to IntegerType");
            System.exit(0);
        }
        return null;
    }

    public Type visit(And n) {
        /* And tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		Type leftType = n.e1.accept(this);  // Tipo da expressão à esquerda
		Type rigthType = n.e2.accept(this); // Tipo da expressão à direita
        Type boolType = new BooleanType();  // Tipo esperado

        if (!this.symbolTable.compareTypes(leftType, boolType) || !this.symbolTable.compareTypes(rigthType, boolType)) {
            // Pelo menos um dos operadores não é booleano: informa erro
            System.err.println("Error" + getFullScope() + ": bad operand types for binary operator 'AND'");
            System.exit(0);
        }
        return new BooleanType();
    }

	public Type visit(LessThan n) {
        /* LessThan tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		Type leftType = n.e1.accept(this);  // Tipo da expressão à esquerda
		Type rigthType = n.e2.accept(this); // Tipo da expressão à direita
        Type intType = new IntegerType();   // Tipo esperado

        if (!this.symbolTable.compareTypes(leftType, intType) || !this.symbolTable.compareTypes(rigthType, intType)) {
            // Pelo menos um dos operadores não é inteiro: informa erro
            System.err.println("Error" + getFullScope() + ": bad operand types for binary operator 'LESS THAN'");
            System.exit(0);
        }
        // return null;
        return new BooleanType();
    }

	public Type visit(Plus n) {
        /* Plus tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		Type leftType = n.e1.accept(this);  // Tipo da expressão à esquerda
		Type rigthType = n.e2.accept(this); // Tipo da expressão à direita
        Type intType = new IntegerType();   // Tipo esperado

        if (!this.symbolTable.compareTypes(leftType, intType) || !this.symbolTable.compareTypes(rigthType, intType)) {
            // Pelo menos um dos operadores não é inteiro: informa erro
            System.err.println("Error" + getFullScope() + ": bad operand types for binary operator 'PLUS'");
            System.exit(0);
        }
        return new IntegerType();
    }

	public Type visit(Minus n) {
        /* Minus tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		Type leftType = n.e1.accept(this);  // Tipo da expressão à esquerda
		Type rigthType = n.e2.accept(this); // Tipo da expressão à direita
        Type intType = new IntegerType();   // Tipo esperado

        if (!this.symbolTable.compareTypes(leftType, intType) || !this.symbolTable.compareTypes(rigthType, intType)) {
            // Pelo menos um dos operadores não é inteiro: informa erro
            System.err.println("Error" + getFullScope() + ": bad operand types for binary operator 'MINUS'");
            System.exit(0);
        }
        return new IntegerType();
    }

	public Type visit(Times n) {
        /* Times tem dois argumentos:
           1) Exp e1 -> Expressão à esquerda
           2) Exp e2 -> Expressão à direita  
        */
		Type leftType = n.e1.accept(this);  // Tipo da expressão à esquerda
		Type rigthType = n.e2.accept(this); // Tipo da expressão à direita
        Type intType = new IntegerType();   // Tipo esperado

        if (!this.symbolTable.compareTypes(leftType, intType) || !this.symbolTable.compareTypes(rigthType, intType)) {
            // Pelo menos um dos operadores não é inteiro: informa erro
            System.err.println("Error" + getFullScope() + ": bad operand types for binary operator 'TIMES'");
            System.exit(0);
        }
        return new IntegerType();
    }

	public Type visit(ArrayLookup n) {
        /* ArrayLookup tem dois argumentos:
           1) Exp e1 -> Nome do array
           2) Exp e2 -> Indice do array  
        */
		Type varType = n.e1.accept(this);   // Tipo da variável que representa array
		Type indexType = n.e2.accept(this); // Tipo da expressão passada como ídice
        if (!this.symbolTable.compareTypes(varType, new IntArrayType())) {
            // Expressão que representa array não é IntArrayType: informa erro
            System.err.println("Error" + getFullScope() + ": IntArrayType required, but " + this.getTypeName(varType) + " found");
            System.exit(0);
        }
        if (!this.symbolTable.compareTypes(indexType, new IntegerType())) {
            // Expressão que representa índice não é IntegeType: informa erro
            System.err.println("Error" + getFullScope() + ": incompatible types: " + this.getTypeName(indexType) + " cannot be converted to IntegerType");
            System.exit(0);
        }
        return new IntegerType();
    }

	public Type visit(ArrayLength n) {
        /* ArrayLength tem um argumento:
           1) Exp e -> Nome do array
        */
		Type varType = n.e.accept(this); // Tipo da expressão que representa o nome do array

		if(!this.symbolTable.compareTypes(varType, new IntArrayType())) {
            // Expressão que representa array não é IntArrayType: informa erro
			System.err.println("Error" + getFullScope() + ": IntArrayType required, but " + this.getTypeName(varType) + " found");
			System.exit(0);
		}
		return new IntegerType();
    }

	public Type visit(Call n) {
        /* Call tem três argumentos:
           1) Exp e        -> Nome da classe
           2) Identifier i -> Nome do método
           3) ExpList el   -> Parâmetros do método
        */
        Type to = n.e.accept(this); // Tipo do objeto

        if (to instanceof IdentifierType) {
            Class cCall = this.symbolTable.getClass(((IdentifierType) to).s);
            Method mCall = this.symbolTable.getMethod(n.i.toString(), cCall.getId());

            Class currClassBK = this.currClass; // Apenas para backup
            this.currClass = cCall;
            this.isMethod = true;
            Type methType = n.i.accept(this);
            this.isMethod = false;
            this.currClass = currClassBK;
            
            int i = 0;
            while (i < n.el.size()) {
                /* n.el é uma lista contendo os parâmetros PASSADOS para o método da classe
                   Percorre todos os parâmetros passados na chamada e verifica se eles correspondem
                   com os tipos dos parânmetros esperados pelo método da classe chamada
                */
                Type paramType = n.el.elementAt(i).accept(this); // Tipo do parâmetro passado na chamada
                Variable expecParam = mCall.getParamAt(i);       // Tipo do parâmetro esperado pelo método que foi chamado
                if (expecParam == null) {
                    /* Foram passados argumentos em excesso para a função chamada: há mais parâmetros passados do que
                       o que se espera receber
                    */
                    System.err.println("Error" + getFullScope() + ": method " + mCall.getId() + " in class " + cCall.getId() + " cannot be applied to given types. Formal argument lists differ in length");
                    System.exit(0);
                } else {
                    // Parâmetro passado tem correspondência no método chamado: verificar correspondência de tipo
                    if (!this.symbolTable.compareTypes(expecParam.type(), paramType)) {
                        // Parâmetro passado não tem mesmo tipo do parâmetro esperado: informa erro
                        System.err.println("Error" + getFullScope() + ": incompatible types: " + this.getTypeName(paramType) + " cannot be converted to " + this.getTypeName(expecParam.type()));
                        System.exit(0);
                    }
                }
                i++;
            }
            // Verifica se todos os parâmetros esperados foram passados
            if (mCall.getParamAt(i) != null) {
                // Todos os parâmetros passados foram verificados, mas a função ainda espera mais argumentos: informa erro
				System.err.println("Error" + getFullScope() + ": method " + mCall.getId() + " in class " + cCall.getId() + " cannot be applied to given types. Formal argument lists differ in length");
				System.exit(0);
			}
			return methType; // Retorna o tipo do método (o tipo que ele retorna)
        } else {
            // Chamada de função ocorreu em um objeto que aceita chamada de função (por exemplo, tipos primitivos)
			System.err.println("Error" + getFullScope() + ": IdentifierType required, but " + this.getTypeName(to) + " found");
			System.exit(0);
        }
        return null;
    }

	public Type visit(IntegerLiteral n) { return new IntegerType(); }

	public Type visit(True n) { return new BooleanType(); }

	public Type visit(False n) { return new BooleanType(); }

	public Type visit(IdentifierExp n) {
        // Tipo representado por uma classe: procura na tabela de símbolo
        Type t = symbolTable.getVarType(this.currMethod, this.currClass, n.s);
        return t;
    }

	public Type visit(This n) { return this.currClass.type(); }

	public Type visit(NewArray n) {
        /* NewArray tem um atributo:
           1) Expression e -> Tamanho do array
        */
        Type lenType = n.e.accept(this); // Tipo da variável que representa tamanho do array
		if(!this.symbolTable.compareTypes(lenType, new IntegerType())) {
            // Tamanho do array não é IntegerType: informa erro
			System.err.println("Error" + getFullScope() + ": incompatible types: " + this.getTypeName(lenType) + " cannot be converted to IntegerType");
			System.exit(0);
		}
		return new IntArrayType();
    }

	public Type visit(NewObject n) {
        /* NewObject tem um atributo:
           1) Indentifier i -> Identificador (nome) do objeto
        */
        return n.i.accept(this);
    }

	public Type visit(Not n) {
        /* Not tem um atributo:
           1) Expression e -> Expressão a ser negada
        */
        Type expType = n.e.accept(this);
		Type boolType = new BooleanType();
		if(!this.symbolTable.compareTypes(expType, boolType)) {
			System.err.println("Error" + getFullScope() + ": bad operand type for unary operator 'NOT'");
			System.exit(0);
		}
		return boolType;
    }

	public Type visit(Identifier n) {
        // Identificador pode ser variável, método ou classe
        if (this.isVariable) {
            // Retorna o tipo da variável associada à classe e método atuais
            return this.symbolTable.getVarType(this.currMethod, this.currClass, n.toString());
        } else if (this.isMethod) {
            // Retorna tipo do método associado à classe atual
            return this.symbolTable.getMethodType(n.toString(), this.currClass.getId());
        } else {
            // Retorna o tipo da classe
            Class c = this.symbolTable.getClass(n.toString());
            if (c == null) {
                // Caso em que identificador não está associado nem a método, nem a classe e nem a variável: não está declarado
                System.err.println("Error" + getFullScope() + ": cannot find symbol " + n.toString());
                System.exit(0);
            }
            return c.type();
        }
    }

    // Função que dado um tipo t, retorna seu nome (em string)
    private String getTypeName(Type t) {
        if (t instanceof IdentifierType) {
            return ((IdentifierType) t).s;
        }
        if (t != null) {
            // t é classe: retorna seu nome
            return t.getClass().getSimpleName();
        } else {
            return "null";
        }
    }

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