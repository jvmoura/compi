package syntaxtree;
import VisitorIR.IRVisitor;
import visitor.IVisitor;

public class Block extends Statement {
  public StatementList sl;

  public Block(StatementList asl) {
    sl=asl;
  }

  @Override
  public <T> T accept(IVisitor<T> visitor) {
      return visitor.visit(this);
  }
  public FragAux.Exp accept(IRVisitor visitor) {
      return visitor.visit(this);
  }
}

