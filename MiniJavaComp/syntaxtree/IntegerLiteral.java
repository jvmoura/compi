package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public class IntegerLiteral extends Exp {
  public int i;

  public IntegerLiteral(int ai) {
    i=ai;
  }

  @Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}

  public FragAux.Exp accept(IRVisitor visitor) {
      return visitor.visit(this);
  }
}
