package syntaxtree;

import visitor.IVisitor;
import VisitorIR.IRVisitor;

public class Identifier {
    public String s;

    public Identifier(String as) {
        s = as;
    }

    public String toString() {
        return s;
    }

    public <T> T accept(IVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public FragAux.Exp accept(IRVisitor visitor) {
        return visitor.visit(this);
    }
}
