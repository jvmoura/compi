package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public abstract class Exp {
  public abstract <T> T accept(IVisitor<T> visitor);
	
	public abstract FragAux.Exp accept(IRVisitor visitor);
}
