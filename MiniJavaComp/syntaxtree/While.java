package syntaxtree;
import VisitorIR.IRVisitor;
import visitor.IVisitor;

public class While extends Statement {
  public Exp e;
  public Statement s;

  public While(Exp ae, Statement as) {
    e=ae; s=as; 
  }

  @Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}

    public FragAux.Exp accept(IRVisitor visitor) {
        return visitor.visit(this);
    }
}

