package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public class IdentifierExp extends Exp {
  public String s;
  public IdentifierExp(String as) { 
    s=as;
  }

  @Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}

  public FragAux.Exp accept(IRVisitor visitor) {
      return visitor.visit(this);
  }
}
