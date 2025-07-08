package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public abstract class Type {
  public abstract <T> T accept(IVisitor<T> visitor);

	public abstract FragAux.Exp accept(IRVisitor visitor);
}
