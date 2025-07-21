package FragAux;

public class Exp {
    public Tree.Expr exp;

    public Exp(Tree.Expr e) {
        exp = e;
    }

    public Tree.Expr unEx() {
        return exp;
    }
}
