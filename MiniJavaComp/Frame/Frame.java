package Frame;
import java.util.List;

import Temp.Label;
import Symbol.Symbol;
import Temp.Temp;
import Tree.Expr;
import Tree.Stm;
import Tree.StmList;
import Assem.Instr;
import Assem.InstrList;
import Temp.TempMap;

public abstract class Frame implements TempMap {
    public Label name;
    public List<Access> formals;
    public abstract Frame newFrame(Symbol name, List<Boolean> formals);
    public abstract Access allocLocal(boolean escape);
    public abstract Temp FP();
    public abstract int wordSize();
    public abstract Expr externalCall(String func, List<Expr> args);
    public abstract Temp RV();
    public abstract String string(Label label, String value);
    public abstract Label badPtr();
    public abstract Label badSub();
    public abstract String tempMap(Temp temp);
    //public abstract List<Instr> codegen(List<Stm> stms);
    public abstract InstrList codegen(StmList stmList);
    public abstract void procEntryExit1(List<Stm> body);
    public abstract void procEntryExit2(List<Instr> body);
    public abstract void procEntryExit3(List<Instr> body);
    public abstract Temp[] registers();
    public abstract void spill(List<Instr> insns, Temp[] spills);
    public abstract String programTail(); //append to end of target code
}
