package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public class This extends Exp {
  @Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}

  public FragAux.Exp accept(IRVisitor visitor) {
      return visitor.visit(this);
  }
}
