/******************************************************** 
DEFINICAO DE VARIAVEL PARA SER USADA NA TABELA DE SIMBOLOS
*********************************************************/

package symboltable;

import syntaxtree.Type;

public class Variable {
    String id; // Nome
    Type type; // Tipo

    public Variable(String id, Type type){
        this.id = id;
        this.type = type;
    }

	public String id() {
		return id;
	}

	public Type type() {
		return type;
	}
}