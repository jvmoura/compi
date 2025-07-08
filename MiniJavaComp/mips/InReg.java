package mips;

import Frame.Access;
import Temp.Temp;
import Tree.Expr;
import Tree.TEMP;

public class InReg extends Access {
    Temp temp;
    InReg(Temp t) {
	    temp = t;
    }

    public Expr exp(Expr fp) {
        return new TEMP(temp);
    }

    public String toString() {
        return temp.toString();
    }
}
