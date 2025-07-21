package Tree;
public class ExpList {
  public Expr head;
  public ExpList tail;
  public ExpList(Expr h, ExpList t) {head=h; tail=t;}

  public <T> ExpList(T h, ExpList t) {
        if (h instanceof Expr) {
            head = (Expr)h;
            tail = t;
        }
    }
}



