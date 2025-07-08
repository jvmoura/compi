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
import syntaxtree.VarDecl;
import syntaxtree.While;

public interface IVisitor<T> {
    /* Define função visit para cada uma das produções da gramática */
    public T visit(Program n);

    // CLASSES
    public T visit(MainClass n);

    public T visit(ClassDeclSimple n);

    public T visit(ClassDeclExtends n);

    // VARIAVEIS
    public T visit(VarDecl n);

    // METODOS
    public T visit(MethodDecl n);

    // PARAMETROS DOS MÉTODOS
    public T visit(Formal n); // Parâmetros da função

    // TIPOS
    public T visit(IntArrayType n);

    public T visit(BooleanType n);

    public T visit(IntegerType n);

    public T visit(IdentifierType n);

    // 
    public T visit(Block n);

    public T visit(If n);

    public T visit(While n);

	public T visit(Print n);

	public T visit(Assign n); // var = valor

	public T visit(ArrayAssign n); // array[i] = j

    public T visit(And n);

	public T visit(LessThan n);

	public T visit(Plus n);

	public T visit(Minus n);

	public T visit(Times n);

	public T visit(ArrayLookup n); // array[0]

	public T visit(ArrayLength n);

	public T visit(Call n); // Chamada de método

	public T visit(IntegerLiteral n);

	public T visit(True n);

	public T visit(False n);

	public T visit(IdentifierExp n);

	public T visit(This n);

	public T visit(NewArray n); // new int[tam]

	public T visit(NewObject n); // new Identifier()

	public T visit(Not n);

	public T visit(Identifier n);
}
