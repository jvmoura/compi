package syntaxtree;
import visitor.IVisitor;
import VisitorIR.IRVisitor;

public abstract class ClassDecl {
  public abstract <T> T accept(IVisitor<T> visitor);
	
	public abstract FragAux.Exp accept(IRVisitor visitor);
}
