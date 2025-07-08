package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public class ArrayLookup extends Exp {
  public Exp e1,e2;
  
  public ArrayLookup(Exp ae1, Exp ae2) { 
    e1=ae1; e2=ae2;
  }

  @Override
  public <T> T accept(IVisitor<T> visitor) {
      return visitor.visit(this);
  }
  public FragAux.Exp accept(IRVisitor visitor) {
      return visitor.visit(this);
  }
}
