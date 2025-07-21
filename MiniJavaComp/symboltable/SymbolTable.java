package symboltable;

import java.util.Hashtable;

import syntaxtree.BooleanType;
import syntaxtree.IdentifierType;
import syntaxtree.IntArrayType;
import syntaxtree.IntegerType;
import syntaxtree.Type;

public class SymbolTable {
    private Hashtable<Object, Object> symbolTable; // Mapeia identificadores (string) a seus siginificados (int, classe, método)

    public SymbolTable() {
        symbolTable = new Hashtable<Object, Object>();
    }

    // Adiciona classe à symbolTable, caso ela já não esteja declarada
    public boolean addClass(String id, String parent) {
        if (containsClass(id)) {
            // Classe já foi declarada no escopo atual
            return false;
        } else {
            symbolTable.put(id, new Class(id, parent));
            return true;
        }
    }

	public Class getClass(String id) {
		if (containsClass(id))
			return (Class) symbolTable.get(id);
		else
			return null;
	}

	public boolean containsClass(String id) {
		return symbolTable.containsKey(id);
	}

    /* Verifica o tipo de uma variável id definida:
       1) no método m da classe c (seja como local ou como parâmetro); OU
       2) no corpo da classe c (como global da classe c) ou no corpo das super classes (como global das super classes)
    */
    public Type getVarType(Method m, Class c, String id) {
        String fullScope = "(" + c.getId() + "$"; // Armazena o escopo completo onde variável está sendo buscada: apenas para saída do erro

        if (m != null) {
            fullScope += m.getId() + ")";

            // Busca variável nos parâmetros ou no corpo do método m
            if (m.getVar(id) != null) {
                // Variável aparece nas variáveis globais do método m: retorna seu tipo
                return m.getVar(id).type();
            }
            if (m.getParam(id) != null) {
                // Variável aparece nos parâmentros do método m: retorna seu tipo
                return m.getParam(id).type();
            }
        }

        /* Se chegar aqui, então variável não está definida nem no corpo nem nos parâmetros do método m. */

        while (c != null) {
            // Procura variável no corpo da classe c (global da classe c) ou nas classes superiores
            if (c.getVar(id) != null) {
                // Variável aparece como global da classe c: retorna seu tipo
                return c.getVar(id).type();
            } else {
                // Variável não aparece na classe c: verifica as super classes, se houver
                if (c.parent() == null) {
                    c = null; // Para sair do laço while
                } else {
                    // Atualiza c para receber o nome da sua super classe imediata (classe mãe)
                    c = getClass(c.parent());
                }
            }
        }

        /* Se chegar aqui, então variável não foi encontrada nem no método m
           nem no corpo da classe c, nem no corpo de nenhuma das classes mãe,
           logo, variável não está definida no escopo atual: informa isso 
         */

         System.err.println("Error " + fullScope + ": variable " + id + " is not defined in current scope");
         System.exit(0);
         return null;
    }

    /* Verifica se um método m com id = id está definido no escopo de uma classe com id = classScope
    ou em algumas de suas super classes, caso existam. Se método estiver definido, retorna-o.
    */
    public Method getMethod(String id, String classScope) {
        if (getClass(classScope) == null) {
            // Classe com id = classScope não está definida
            System.err.println("Error: class " + classScope + " is not defined");
            System.exit(0);
        }

        /* Se chegar aqui, classe está definida: obtém a classe para verficar seus métodos */
        Class c = getClass(classScope);
        while(c != null) {
            if (c.getMethod(id) != null) {
                // Método está definido na classe c, retorna-o
                return c.getMethod(id);
            } else {
                // Procura na super classe, se houver
                if (c.parent() == null) {
                    c = null; // Classe não tem super classe, sai do laço
                } else {
                    // Atualiza c para a super classe
                    c = getClass(c.parent());
                }
            }
        }
        /* Se chegar aqui, método não está definido nem na classe classScope nem em nenhuma
           de suas super classes: informa isso
         */
        System.err.println("Error: method " + id + " is not defined in the " + classScope + " class or in any of its super classes");
        System.exit(0);
        return null;
    }

   /* Verifica se um método m com id = id está definido no escopo de uma classe com id = classScope
    ou em algumas de suas super classes, caso existam. Se método estiver definido, retorna seu tipo.
    */
	public Type getMethodType(String id, String classScope) {
		if (getClass(classScope) == null) {
			System.err.println("Error: class " + classScope + " is not defined");
			System.exit(0);
		}

		Class c = getClass(classScope);
		while (c != null) {
			if (c.getMethod(id) != null) {
				return c.getMethod(id).getType();
			} else {
				if (c.parent() == null) {
					c = null;
				} else {
					c = getClass(c.parent());
				}
			}
		}

        System.err.println("Error: method " + id + " is not defined in the " + classScope + " class or in any of its super classes");
		System.exit(0);
		return null;
	}

    /* Dados dois tipos, retorna true se:
       1) Tipos básicos:
           1.1) Ambos são inteiros;
           1.2) Ambos são booleanos;
           1.3) Ambos são array de inteiro.
        2) Tipos 'complexos' (classes):
            2.1 Ambas são classes equivalentes.
     */
    public boolean compareTypes(Type t1, Type t2) {
        if (t1 == null || t2 == null) {
            return false;
        }

        if (t1 instanceof IntegerType && t2 instanceof IntegerType) {
            // Ambos os tipos são inteiros
            return true;
        }
        if (t1 instanceof BooleanType && t2 instanceof BooleanType) {
            // Ambos os tipos são booleanos
            return true;
        }
		if (t1 instanceof IntArrayType && t2 instanceof IntArrayType) {
            // Ambos os tipos são array de inteiro
			return true;
        }

        // Verificando tipos 'complexos': classes
        if (t1 instanceof IdentifierType && t2 instanceof IdentifierType) {
			IdentifierType i1 = (IdentifierType) t1;
			IdentifierType i2 = (IdentifierType) t2;

            Class c = getClass(i2.s);
            while (c != null) {
                if (i1.s.equals(c.getId())) {
                    /* Campo s do IdentifierType representado por i1 é igual ao campo
                       id da classe, logo os tipos são iguais
                     */
                    return true;
                } else {
                    // Verifica para as super classes
                    if (c.parent() == null) {
                        // Não há super classe: sai do laço while
                        return false;
                    }
                    // Atualiza c para verificar na super classe
                    c = getClass(c.parent());
                }
            }
        }
        return false;
    }
}
