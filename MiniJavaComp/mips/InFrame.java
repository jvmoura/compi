package mips;

import Frame.Access;
import Tree.Expr;
import Tree.BINOP;
import Tree.CONST;
import Tree.MEM;

public class InFrame extends Access {
    int offset;
    InFrame(int o) {
	offset = o;
    }

    public Expr exp(Expr fp) {
        return new MEM
	    (new BINOP(BINOP.PLUS, fp, new CONST(offset)));
    }

    public String toString() {
        Integer offset = new Integer(this.offset);
	return offset.toString();
    }
}
