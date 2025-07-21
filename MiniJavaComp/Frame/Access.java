package Frame;

import Tree.Expr;

public abstract class Access {
  public abstract String toString();
  public abstract Tree.Expr exp(Tree.Expr e);
}
