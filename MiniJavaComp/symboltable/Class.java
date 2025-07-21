package symboltable;
/******************************************************** 
DEFINICAO DA CLASSE PARA SER USADA NA TABELA DE SIMBOLOS
*********************************************************/

import java.util.Enumeration;
import java.util.Hashtable;

import syntaxtree.IdentifierType;
import syntaxtree.Type;

public class Class {
	String id; // Nome da classe
	Hashtable<Object, Method> methods; // Métodos da classe: [id, informações do método]
	Hashtable<Object, Variable> globals; // Variáveis globais da classe: [id, informações da variável]
	String parent; // Super classe, caso haja herança
	Type type; // Tipo da classe

	public Class(String id, String p) {
		this.id = id;
		parent = p;
		type = new IdentifierType(id);
		methods = new Hashtable<Object, Method>();
		globals = new Hashtable<Object, Variable>();
	}

	public Class() {}

	public String getId() {
		return id;
	}

	public Type type() {
		return type;
	}

    public boolean addMethod(String id, Type type) {
		if (containsMethod(id)) {
            // Método já definido no escopo atual
			return false;
        } else {
			methods.put(id, new Method(id, type));
			return true;
		}
	}

	public Enumeration<Object> getMethods() {
		return methods.keys();
	}

	public Method getMethod(String id) {
		if (containsMethod(id))
			return (Method) methods.get(id);
		else
			return null;
	}

	public boolean addVar(String id, Type type) {
		if (globals.containsKey(id))
			return false;
		else {
			globals.put(id, new Variable(id, type));
			return true;
		}
	}

	public Variable getVar(String id) {
		if (containsVar(id))
			return (Variable) globals.get(id);
		else
			return null;
	}

	public Hashtable<Object, Variable> getAllVars() {
		return this.globals;
	}

	public boolean containsVar(String id) {
		return globals.containsKey(id);
	}

	public boolean containsMethod(String id) {
		return methods.containsKey(id);
	}

	public String parent() {
		return parent;
	}
}