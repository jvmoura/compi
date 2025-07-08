package Assem;

import Frame.Frame;
import Tree.*;
import Temp.*;

public class Codegen {
    Frame frame;
    
    public Codegen(Frame f) {
        frame = f;
    }

    private InstrList ilist=null, last=null;

    private void emit(Instr inst) {
        if (last!=null)
        {
            last = last.tail = new InstrList(inst,null);
        }
        else
        {
            last = ilist = new InstrList(inst,null);
        }
    }

    public InstrList codegen(Stm s) {
        InstrList l;
        munchStm(s);
        l=ilist;
        ilist=last=null;
        return l;
    }

    void munchStm(Stm s) {
        if (s instanceof SEQ)
        {
            munchStm(((SEQ)s).left);
            munchStm(((SEQ)s).right);
        }
        else if(s instanceof Tree.MOVE)
        {
            munchMOVE((Tree.MOVE)s);
        }
        else if(s instanceof Tree.LABEL)
        {
            munchLABEL((Tree.LABEL)s);
        }
        else if(s instanceof CJUMP)
        {
            munchCJUMP((CJUMP)s) ;
        }
        else if(s instanceof JUMP)
        {
            munchJUMP((JUMP)s);
        }
        else if(s instanceof EXP)
        {
            munchCALL((CALL)((EXP)s).exp);
        }
    }

    void munchMOVE(Tree.MOVE s) {
        if (s.dst instanceof MEM && s.src instanceof MEM) {
            Temp tempDst = munchExp(((MEM) s.dst).exp);
            Temp tempSrc = munchExp(((MEM) s.src).exp);
            TempList srcList = new TempList(tempSrc, null);
            TempList dstList = new TempList(tempDst, srcList);
    
            emit(new OPER("MOVE M[`s0] <- M[`s1]\n", null, dstList));
            return;
        }
    
        if (s.dst instanceof MEM && s.src instanceof Expr) {
            MEM memDst = (MEM) s.dst;
    
            if (memDst.exp instanceof BINOP) {
                BINOP binop = (BINOP) memDst.exp;
    
                if (binop.binop == BINOP.PLUS) {
                    if (binop.right instanceof CONST) {
                        Temp t1 = munchExp(binop.left);
                        Temp t2 = munchExp(s.src);
                        emit(new OPER("STORE M[`s0 +" + ((CONST) binop.right).value + "] <- `s1\n", new TempList(t1, null), new TempList(t2, null)));
                        return;
                    } else if (binop.left instanceof CONST) {
                        Temp t1 = munchExp(binop.right);
                        Temp t2 = munchExp(s.src);
                        emit(new OPER("STORE M[`s0 +" + ((CONST) binop.left).value + "] <- `s1\n", new TempList(t1, null) , new TempList(t2, null)));
                        return;
                    }
                }
            } 
    
            if (memDst.exp instanceof CONST) {
                TempList srcList = new TempList(munchExp(s.src), null);
                //"STORE M[r0+" + i + "] <- 's0\n"
                emit(new OPER("STORE M[r0 +" + ((CONST) memDst.exp).value + "] <- `s0\n", null, srcList));
                return;

            } 
            TempList dstList = new TempList(munchExp(memDst.exp), null);
            TempList srcList = new TempList(munchExp(s.src), null);
            emit(new OPER("STORE M[`s0] <- `s1\n", dstList, srcList));
            return;
        }
        if (s.dst instanceof TEMP) {
            TempList dstList = new TempList(((TEMP) s.dst).temp, null);
            TempList srcList = new TempList(munchExp(s.src), null);
            emit(new OPER("ADD 'd0 <- 's0 + r0\n", dstList, srcList));
        }
    }
    
    void munchLABEL(Stm s)
    {
        emit(new LABEL(((Tree.LABEL)s).label.toString() + ":", ((Tree.LABEL)s).label));
    }

    void munchCJUMP(CJUMP s) {
        Temp l = munchExp(s.left);
        Temp r = munchExp(s.right);
    
        TempList tempList = new TempList(l, new TempList(r, null));

        String branch = "";
        switch (s.relop) {
            case CJUMP.GE:
                branch = "BRANCHGE if `s0 >= 0 goto `j0\n";
                break;
            case CJUMP.EQ:
                branch = "BRANCHEQ if `s0 = 0 goto `j0\n";
                break;
            case CJUMP.NE:
                branch = "BRANCHNE if `s0 != 0 goto `j0\n";
                break;
            case CJUMP.LT:
                branch = "BRANCHLT if `s0 < 0 goto `j0\n";
                break;
        }
        // salto condicional
        emit(new OPER(branch, null, tempList, new LabelList(s.iftrue, null)));
        // salto incondicional
        emit(new OPER("JUMP `j0\n", null, null, new LabelList(s.iffalse, null)));
    }
    
    void munchJUMP(Stm s)
    {   
        OPER op = new OPER("JUMP `s0\n",null,null,((JUMP)s).targets);
        emit(op);
    }

    void munchCALL(CALL s){
        Temp r      = munchExp(s.func); 
        TempList l  = munchARGS(0,s.args);
        NAME name        = (NAME)s.func;
        TempList l1 = new TempList(r,l);

        OPER op = new OPER("JUMP "+name.label+"",null,l1);
        emit(op);
    }

    TempList munchARGS(int i, ExpList args) {
        ExpList temp = args;
        TempList ret = null;
        while(!(temp == null)){
            Temp temp_head = munchExp(temp.head);
            ret = new TempList(temp_head, ret);
            temp = temp.tail;
        }
        return ret;
    }
    
    Temp munchExp(Expr e)
    {
        if (e instanceof MEM){
            return munchMEM((MEM)e);
        }
        else if (e instanceof BINOP){
            return munchBINOP((BINOP)e);
        }
        else if (e instanceof CONST){
            return munchCONST((CONST)e);
        }
        else if (e instanceof TEMP){
            return munchTEMP((TEMP)e);
        }
        return null;
    }

    Temp munchMEM(MEM e){
        Temp t = new Temp();
        TempList l1 = new TempList(t,null);
        if (e.exp instanceof BINOP && ((BINOP)e.exp).binop == BINOP.PLUS)
        {
            Expr r = ((BINOP)e.exp).right;
            Expr l = ((BINOP)e.exp).left;

            if(r instanceof CONST)
            {
                TempList s = new TempList(munchExp(l),null);
                emit(new OPER("LOAD 'd0 <- M['s0 + " + ((CONST)r).value + "]\n", l1 ,s));
            }
            else if(l instanceof CONST)
            {
                TempList s = new TempList(munchExp(r),null);
                emit(new OPER("LOAD 'd0 <- M['s0 + " + ((CONST)l).value + "]\n", l1 ,s));
            }

        }
        else if (e.exp instanceof CONST)
        {
            CONST c = (CONST)e.exp;
            //"LOAD 'd0 <- M[r0+" + i + "]\n"
            emit(new OPER("LOAD 'd0 <- M[r0 + " +c .value + "]\n", l1 ,null));
        }
        else
        {
            emit(new OPER("LOAD `d0 <- M[`s0 + 0]\n", l1 , new TempList(munchExp(e.exp),null )));
        }
        return t;    
    }
   
    Temp munchBINOP(BINOP e){
        Temp r = new Temp();
        TempList l1 = new TempList(r,null);

        if (e.binop == BINOP.PLUS){
            if (e.left instanceof CONST){
              TempList s = new TempList(munchExp(e.right),null);
              OPER op = new OPER("ADDI 'd0 <- 's0 + " + ((CONST)e.left).value + "",l1,s);
              emit(op);
            }
            else if (e.right instanceof CONST){
              TempList s = new TempList(munchExp(e.left),null);
              OPER op = new OPER("ADDI 'd0 <- 's0 + " + ((CONST)e.right).value + "", l1 , s);
                    emit(op);
            }
            else{
                TempList s = new TempList(munchExp(e.left), new TempList(munchExp(e.right),null));
                OPER op = new OPER("ADD `d0 <- `s0 + `s1 \n",l1, s);
                emit(op);
                }
        }
        else if(e.binop == BINOP.MINUS)
        {
            TempList l3 = new TempList(munchExp(e.right),null);
            TempList l2 = new TempList(munchExp(e.left),l3);
            OPER op = new OPER("SUB `d0 <- `s0 - `s1 \n", l1, l2);
            emit(op);
        }
        else if(e.binop == BINOP.DIV)
        {
            TempList l3 = new TempList(munchExp(e.right),null);
            TempList l2 = new TempList(munchExp(e.left),l3);
            OPER op = new OPER("DIV `d0 <- `s0 / `s1\n", l1, l2);
            emit(op);
        }
        else if(e.binop == BINOP.MUL)
        {
            TempList l3 = new TempList(munchExp(e.right),null);
            TempList l2 = new TempList(munchExp(e.left),l3);
            OPER op = new OPER("MUL `d0 <- `s0 * `s1\n", l1, l2);
            emit(op);
        }
        else
        {
            TempList l3 = new TempList(munchExp(e.right),null);
            TempList l2 = new TempList(munchExp(e.left),l3);
            OPER op = new OPER("AND `d0 <- `s0 & `s1\n", l1, l2);
            emit(op);
        }
        return r;
    }

    Temp munchCONST(CONST e){
        Temp r = new Temp();
        TempList l  = new TempList(r,null);
        emit(new OPER("ADDI 'd0 <- r0 + " + e.value + "\n", null, l));
        return r;
    }

    Temp munchTEMP(TEMP e){
        return e.temp;
    }
}
