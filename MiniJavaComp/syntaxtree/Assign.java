package syntaxtree;
import VisitorIR.IRVisitor;
import visitor.IVisitor;

public class Assign extends Statement {
  public Identifier i;
  public Exp e;

  public Assign(Identifier ai, Exp ae) {
    i=ai; e=ae; 
  }

  @Override
  public <T> T accept(IVisitor<T> visitor) {
      return visitor.visit(this);
  }
  public FragAux.Exp accept(IRVisitor visitor) {
      return visitor.visit(this);
  }
}

