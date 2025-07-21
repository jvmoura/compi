package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public class NewObject extends Exp {
  public Identifier i;
  
  public NewObject(Identifier ai) {
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
