package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public class ArrayLength extends Exp {
  public Exp e;
  
  public ArrayLength(Exp ae) {
    e=ae; 
  }

  @Override
  public <T> T accept(IVisitor<T> visitor) {
      return visitor.visit(this);
  }
  public FragAux.Exp accept(IRVisitor visitor) {
      return visitor.visit(this);
  }
}
