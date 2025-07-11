/* Generated By:JavaCC: Do not edit this line. MiniJavaParser.java */
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import syntaxtree.*;
import visitor.BuildSymbolTableVisitor;
import visitor.TypeCheckVisitor;
import visitor.PrettyPrintVisitor;
import mips.MipsFrame;
import VisitorIR.BuildIRVisitor;
import Canon.Canon;
import Canon.BasicBlocks;
import Canon.TraceSchedule;
import Tree.PrintTree;
import Assem.*;

// Classe de entrada da expressão e retorno da saída
public class MiniJavaParser implements MiniJavaParserConstants {
    public static void main(String []args) throws ParseException, IOException {
        // Recebe um nome de arquivo na entrada e direciona seu conteúdo ao parser
        MiniJavaParser parser = new MiniJavaParser(new FileInputStream(args[0]));
        Program p = parser.Program();

        // Constrói tabela de símbolos
        BuildSymbolTableVisitor buildSTVisitor = new BuildSymbolTableVisitor();
        buildSTVisitor.visit(p);

        // Realiza checagem de tipos com base na tabela de símbolos
        TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor(buildSTVisitor.getSymbolTable());
        typeCheckVisitor.visit(p);

        // Geração de código intermediário
        MipsFrame frame = new MipsFrame();
        BuildIRVisitor buildIRVisitor = new BuildIRVisitor(frame, buildSTVisitor.getSymbolTable());
        buildIRVisitor.visit(p);

        // Geração do código canônico
        FragAux.Frag next_frag = buildIRVisitor.frags.getNext();
        List<TraceSchedule> trace_list = new ArrayList<TraceSchedule>();
        //PrintTree printer = new PrintTree(System.out);

        while(next_frag.hasNext())
        {
            Tree.Stm body = ((FragAux.ProcFrag)next_frag).body;
            TraceSchedule trace_schedule = new TraceSchedule(new BasicBlocks(Canon.linearize(body)));
            trace_list.add(trace_schedule);

            Tree.StmList stml = trace_schedule.stms;

            while(stml != null)
            {
            //printer.prStm(stml.head);
            stml = stml.tail;
            }
            //System.out.println(); // Imprime uma linha vazia para separar os blocos

            next_frag = next_frag.getNext();
        }

        Tree.Stm body = ((FragAux.ProcFrag)next_frag).body;
        TraceSchedule trace_schedule = new TraceSchedule(new BasicBlocks(Canon.linearize(body)));
        trace_list.add(trace_schedule);

        Tree.StmList stml = trace_schedule.stms;

        while(stml != null)
        {
        //printer.prStm(stml.head);
        stml = stml.tail;
        }
        //System.out.println(); // Imprime uma linha vazia para separar os blocos

        // Seleção de instruções
        Iterator<TraceSchedule> itO = trace_list.iterator();
        InstrList instL = null;

        while (itO.hasNext()) {
            InstrList instrList = frame.codegen(itO.next().stms);
            for (InstrList i = instrList; i.tail != null; i = i.tail) {
                instL = new InstrList(i.head, instL);
            }
        }

        // Imprimir a lista de instruções
        InstrList aux = instL;
        while (aux != null) {
            System.out.print(aux.head.assem);
            System.out.println("\n");
            aux = aux.tail;
        }

    }

/*---------------------------------------------
Definição da gramática - Regras de Produção
---------------------------------------------*/
  final public Program Program() throws ParseException {
    MainClass m;
    ClassDeclList cl = new ClassDeclList();
    m = MainClass();
    cl = ClassDeclarationList();
    jj_consume_token(0);
      {if (true) return new Program(m, cl);}
    throw new Error("Missing return statement in function");
  }

  final public MainClass MainClass() throws ParseException {
    Identifier name, args;
    Statement s;
    jj_consume_token(CLASS);
    name = Identifier();
    jj_consume_token(LBRACE);
    jj_consume_token(PUBLIC);
    jj_consume_token(STATIC);
    jj_consume_token(VOID);
    jj_consume_token(MAIN);
    jj_consume_token(LPAREN);
    jj_consume_token(STRING);
    jj_consume_token(LSQPAREN);
    jj_consume_token(RSQPAREN);
    args = Identifier();
    jj_consume_token(RPAREN);
    jj_consume_token(LBRACE);
    s = Statement();
    jj_consume_token(RBRACE);
    jj_consume_token(RBRACE);
      {if (true) return new MainClass(name, args, s);}
    throw new Error("Missing return statement in function");
  }

  final public ClassDecl ClassDeclaration() throws ParseException {
    Identifier name, nameSuper;
    VarDeclList vl;
    MethodDeclList ml;
    if (jj_2_1(3)) {
      jj_consume_token(CLASS);
      name = Identifier();
      jj_consume_token(LBRACE);
      vl = VarDeclarationList();
      ml = MethodDeclarationList();
      jj_consume_token(RBRACE);
      {if (true) return new ClassDeclSimple(name, vl, ml);}
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CLASS:
        jj_consume_token(CLASS);
        name = Identifier();
        jj_consume_token(EXTENDS);
        nameSuper = Identifier();
        jj_consume_token(LBRACE);
        vl = VarDeclarationList();
        ml = MethodDeclarationList();
        jj_consume_token(RBRACE);
      {if (true) return new ClassDeclExtends(name, nameSuper, vl, ml);}
      {if (true) return null;}
        break;
      default:
        jj_la1[0] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public ClassDeclList ClassDeclarationList() throws ParseException {
    ClassDecl c;
    ClassDeclList l = new ClassDeclList();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CLASS:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      c = ClassDeclaration();
                              l.addElement(c);
    }
      {if (true) return l;}
    throw new Error("Missing return statement in function");
  }

  final public VarDecl VarDeclaration() throws ParseException {
    Type t;
    Identifier name;
    t = Type();
    name = Identifier();
    jj_consume_token(SEMICOLON);
      {if (true) return new VarDecl(t, name);}
    throw new Error("Missing return statement in function");
  }

  final public VarDeclList VarDeclarationList() throws ParseException {
    VarDecl v;
    VarDeclList vl = new VarDeclList();
    label_2:
    while (true) {
      if (jj_2_2(2)) {
        ;
      } else {
        break label_2;
      }
      v = VarDeclaration();
                                          vl.addElement(v);
    }
      {if (true) return vl;}
    throw new Error("Missing return statement in function");
  }

  final public MethodDecl MethodDeclaration() throws ParseException {
    Type returnType;
    Identifier name;
    FormalList params = new FormalList();
    VarDeclList vl = new VarDeclList();
    StatementList sl = new StatementList();
    Exp returnExp;
    jj_consume_token(PUBLIC);
    returnType = Type();
    name = Identifier();
    jj_consume_token(LPAREN);
    params = ParamsList();
    jj_consume_token(RPAREN);
    jj_consume_token(LBRACE);
    vl = VarDeclarationList();
    sl = StatementList();
    jj_consume_token(RETURN);
    returnExp = Expression();
    jj_consume_token(SEMICOLON);
    jj_consume_token(RBRACE);
      {if (true) return new MethodDecl(returnType, name, params, vl, sl, returnExp);}
    throw new Error("Missing return statement in function");
  }

  final public MethodDeclList MethodDeclarationList() throws ParseException {
    MethodDeclList ml = new MethodDeclList();
    MethodDecl m;
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PUBLIC:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_3;
      }
      m = MethodDeclaration();
                             ml.addElement(m);
    }
    {if (true) return ml;}
    throw new Error("Missing return statement in function");
  }

  final public FormalList ParamsList() throws ParseException {
    FormalList fl = new FormalList();
    Formal f;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INTEGER:
    case BOOLEAN:
    case ID:
      f = Param();
          fl.addElement(f);
      label_4:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case KOMMA:
          ;
          break;
        default:
          jj_la1[3] = jj_gen;
          break label_4;
        }
        jj_consume_token(KOMMA);
        f = Param();
                            fl.addElement(f);
      }
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
      {if (true) return fl;}
    throw new Error("Missing return statement in function");
  }

  final public Formal Param() throws ParseException {
    Type t;
    Identifier name;
    t = Type();
    name = Identifier();
      {if (true) return new Formal(t, name);}
    throw new Error("Missing return statement in function");
  }

  final public Type Type() throws ParseException {
    Type t;
    Identifier id;
    if (jj_2_3(2)) {
      /* Decide entre ArrayType (int []) e IntegerType (int) */
          t = ArrayType();
                      {if (true) return t;}
    } else {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case BOOLEAN:
        t = BooleanType();
                          {if (true) return t;}
        break;
      case INTEGER:
        t = IntegerType();
                          {if (true) return t;}
        break;
      case ID:
        id = Identifier();
                          {if (true) return new IdentifierType(id.toString());}
      {if (true) return null;}
        break;
      default:
        jj_la1[5] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public Type ArrayType() throws ParseException {
    jj_consume_token(INTEGER);
    jj_consume_token(LSQPAREN);
    jj_consume_token(RSQPAREN);
      {if (true) return new IntArrayType();}
    throw new Error("Missing return statement in function");
  }

  final public Type BooleanType() throws ParseException {
    jj_consume_token(BOOLEAN);
      {if (true) return new BooleanType();}
    throw new Error("Missing return statement in function");
  }

  final public Type IntegerType() throws ParseException {
    jj_consume_token(INTEGER);
      {if (true) return new IntegerType();}
    throw new Error("Missing return statement in function");
  }

  final public Statement Statement() throws ParseException {
    Statement s;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LBRACE:
      s = Block();
                  {if (true) return s;}
      break;
    case IF:
      s = IfStatement();
                          {if (true) return s;}
      break;
    case WHILE:
      s = WhileStatement();
                             {if (true) return s;}
      break;
    case PRINT:
      s = PrintStatement();
                             {if (true) return s;}
      break;
    default:
      jj_la1[6] = jj_gen;
      if (jj_2_4(2)) {
        s = AssignStatement();
                                           {if (true) return s;}
      } else if (jj_2_5(2)) {
        s = ArrayAssignStatement();
                                                {if (true) return s;}
      {if (true) return null;}
      } else {
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public StatementList StatementList() throws ParseException {
    Statement s;
    StatementList sl = new StatementList();
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LBRACE:
      case IF:
      case WHILE:
      case PRINT:
      case ID:
        ;
        break;
      default:
        jj_la1[7] = jj_gen;
        break label_5;
      }
      s = Statement();
                        sl.addElement(s);
    }
      {if (true) return sl;}
    throw new Error("Missing return statement in function");
  }

  final public Statement Block() throws ParseException {
    Statement s;
    StatementList sl = new StatementList();
    jj_consume_token(LBRACE);
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LBRACE:
      case IF:
      case WHILE:
      case PRINT:
      case ID:
        ;
        break;
      default:
        jj_la1[8] = jj_gen;
        break label_6;
      }
      s = Statement();
                           sl.addElement(s);
    }
    jj_consume_token(RBRACE);
      {if (true) return new Block(sl);}
    throw new Error("Missing return statement in function");
  }

  final public Statement IfStatement() throws ParseException {
    Exp e;
    Statement s1, s2;
    jj_consume_token(IF);
    jj_consume_token(LPAREN);
    e = Expression();
    jj_consume_token(RPAREN);
    s1 = Statement();
    jj_consume_token(ELSE);
    s2 = Statement();
      {if (true) return new If(e, s1, s2);}
    throw new Error("Missing return statement in function");
  }

  final public Statement WhileStatement() throws ParseException {
    Exp e;
    Statement s;
    jj_consume_token(WHILE);
    jj_consume_token(LPAREN);
    e = Expression();
    jj_consume_token(RPAREN);
    s = Statement();
      {if (true) return new While(e, s);}
    throw new Error("Missing return statement in function");
  }

  final public Statement PrintStatement() throws ParseException {
    Exp e;
    jj_consume_token(PRINT);
    jj_consume_token(LPAREN);
    e = Expression();
    jj_consume_token(RPAREN);
    jj_consume_token(SEMICOLON);
      {if (true) return new Print(e);}
    throw new Error("Missing return statement in function");
  }

  final public Statement AssignStatement() throws ParseException {
    Identifier id;
    Exp e;
    id = Identifier();
    jj_consume_token(ASSIGN);
    e = Expression();
    jj_consume_token(SEMICOLON);
      {if (true) return new Assign(id, e);}
    throw new Error("Missing return statement in function");
  }

  final public Statement ArrayAssignStatement() throws ParseException {
    Identifier id;
    Exp e1, e2;
    id = Identifier();
    jj_consume_token(LSQPAREN);
    e1 = Expression();
    jj_consume_token(RSQPAREN);
    jj_consume_token(ASSIGN);
    e2 = Expression();
    jj_consume_token(SEMICOLON);
      {if (true) return new ArrayAssign(id, e1, e2);}
    throw new Error("Missing return statement in function");
  }

/* PRECEDÊNCIA DE OPERADORES */
  final public Exp Expression() throws ParseException {
    Exp e;
    e = AndExpression();
      {if (true) return e;}
    throw new Error("Missing return statement in function");
  }

  final public Exp AndExpression() throws ParseException {
    Exp x, y;
    x = CompareExpression();
    label_7:
    while (true) {
      if (jj_2_6(2)) {
        ;
      } else {
        break label_7;
      }
      jj_consume_token(AND);
      y = CompareExpression();
       x = new And(x, y);
    }
      {if (true) return x;}
    throw new Error("Missing return statement in function");
  }

  final public Exp CompareExpression() throws ParseException {
    Exp x, y;
    x = PlusExpression();
    if (jj_2_7(2)) {
      jj_consume_token(LTHAN);
      y = PlusExpression();
       x = new LessThan(x, y);
    } else {
      ;
    }
      {if (true) return x;}
    throw new Error("Missing return statement in function");
  }

  final public Exp PlusExpression() throws ParseException {
    Exp x, y;
    x = TimesExpression();
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
      case MINUS:
        ;
        break;
      default:
        jj_la1[9] = jj_gen;
        break label_8;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        jj_consume_token(PLUS);
        y = TimesExpression();
                                 x = new Plus(x, y);
        break;
      case MINUS:
        jj_consume_token(MINUS);
        y = TimesExpression();
                                 x = new Minus(x, y);
        break;
      default:
        jj_la1[10] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
      {if (true) return x;}
    throw new Error("Missing return statement in function");
  }

  final public Exp TimesExpression() throws ParseException {
    Exp x, y;
    x = PrefixExpression();
    label_9:
    while (true) {
      if (jj_2_8(2)) {
        ;
      } else {
        break label_9;
      }
      jj_consume_token(TIMES);
      y = PrefixExpression();
          x = new Times(x, y);
    }
      {if (true) return x;}
    throw new Error("Missing return statement in function");
  }

  final public Exp PrefixExpression() throws ParseException {
    Exp e;
    int c = 0;
    label_10:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        ;
        break;
      default:
        jj_la1[11] = jj_gen;
        break label_10;
      }
      jj_consume_token(NOT);
           ++c;
    }
    e = PostfixExpression();
      for (int i=0; i<c; ++i) e = new Not(e);
      {if (true) return e;}
    throw new Error("Missing return statement in function");
  }

  final public Exp PostfixExpression() throws ParseException {
    Exp e, pr;
    Identifier id;
    ExpList el = new ExpList();
    pr = PrimaryExpression();
    label_11:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LSQPAREN:
      case DOT:
        ;
        break;
      default:
        jj_la1[12] = jj_gen;
        break label_11;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LSQPAREN:
        jj_consume_token(LSQPAREN);
        e = Expression();
        jj_consume_token(RSQPAREN);
                                {if (true) return new ArrayLookup(pr, e);}
        break;
      default:
        jj_la1[14] = jj_gen;
        if (jj_2_9(2)) {
          jj_consume_token(DOT);
          id = Identifier();
          jj_consume_token(LPAREN);
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case LPAREN:
          case TRUE:
          case FALSE:
          case THIS:
          case NEW:
          case NOT:
          case INTEGER_LITERAL:
          case ID:
            ExpressionList(el);
            break;
          default:
            jj_la1[13] = jj_gen;
            ;
          }
          jj_consume_token(RPAREN);
          {if (true) return new Call(pr, id, el);}
        } else if (jj_2_10(2)) {
          jj_consume_token(DOT);
          jj_consume_token(LENGTH);
          {if (true) return new ArrayLength(pr);}
        } else {
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
      {if (true) return pr;}
    throw new Error("Missing return statement in function");
  }

  final public Identifier Identifier() throws ParseException {
  Token t;
    t = jj_consume_token(ID);
               {if (true) return new Identifier(t.image);}
    throw new Error("Missing return statement in function");
  }

  final public Exp PrimaryExpression() throws ParseException {
    Exp e;
    Identifier id;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INTEGER_LITERAL:
      e = IntegerLiteral();
                           {if (true) return e;}
      break;
    case TRUE:
      jj_consume_token(TRUE);
               {if (true) return new True();}
      break;
    case FALSE:
      jj_consume_token(FALSE);
                {if (true) return new False();}
      break;
    case ID:
      id = Identifier();
                          {if (true) return new IdentifierExp(id.toString());}
      break;
    case THIS:
      jj_consume_token(THIS);
               {if (true) return new This();}
      break;
    default:
      jj_la1[15] = jj_gen;
      if (jj_2_11(2)) {
        jj_consume_token(NEW);
        jj_consume_token(INTEGER);
        jj_consume_token(LSQPAREN);
        e = Expression();
        jj_consume_token(RSQPAREN);
        {if (true) return new NewArray(e);}
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case NEW:
          jj_consume_token(NEW);
          id = Identifier();
          jj_consume_token(LPAREN);
          jj_consume_token(RPAREN);
        {if (true) return new NewObject(id);}
          break;
        case LPAREN:
          jj_consume_token(LPAREN);
          e = Expression();
          jj_consume_token(RPAREN);
        {if (true) return e;}
      {if (true) return null;}
          break;
        default:
          jj_la1[16] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
    throw new Error("Missing return statement in function");
  }

  final public Exp IntegerLiteral() throws ParseException {
    Token t;
    int num;
    t = jj_consume_token(INTEGER_LITERAL);
        num = Integer.valueOf(t.image);
        {if (true) return new IntegerLiteral(num);}
    throw new Error("Missing return statement in function");
  }

  final public void ExpressionList(ExpList el) throws ParseException {
    Exp e;
    e = Expression();
                       el.addElement(e);
    label_12:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case KOMMA:
        ;
        break;
      default:
        jj_la1[17] = jj_gen;
        break label_12;
      }
      jj_consume_token(KOMMA);
      e = Expression();
          el.addElement(e);
    }
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  private boolean jj_2_6(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_6(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  private boolean jj_2_7(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_7(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(6, xla); }
  }

  private boolean jj_2_8(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_8(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(7, xla); }
  }

  private boolean jj_2_9(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_9(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(8, xla); }
  }

  private boolean jj_2_10(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_10(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(9, xla); }
  }

  private boolean jj_2_11(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_11(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(10, xla); }
  }

  private boolean jj_3R_18() {
    if (jj_3R_19()) return true;
    return false;
  }

  private boolean jj_3R_37() {
    if (jj_scan_token(LPAREN)) return true;
    return false;
  }

  private boolean jj_3R_36() {
    if (jj_scan_token(NEW)) return true;
    return false;
  }

  private boolean jj_3_11() {
    if (jj_scan_token(NEW)) return true;
    if (jj_scan_token(INTEGER)) return true;
    return false;
  }

  private boolean jj_3R_35() {
    if (jj_scan_token(THIS)) return true;
    return false;
  }

  private boolean jj_3R_34() {
    if (jj_3R_13()) return true;
    return false;
  }

  private boolean jj_3R_33() {
    if (jj_scan_token(FALSE)) return true;
    return false;
  }

  private boolean jj_3R_32() {
    if (jj_scan_token(TRUE)) return true;
    return false;
  }

  private boolean jj_3_6() {
    if (jj_scan_token(AND)) return true;
    if (jj_3R_18()) return true;
    return false;
  }

  private boolean jj_3_5() {
    if (jj_3R_17()) return true;
    return false;
  }

  private boolean jj_3R_28() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_31()) {
    jj_scanpos = xsp;
    if (jj_3R_32()) {
    jj_scanpos = xsp;
    if (jj_3R_33()) {
    jj_scanpos = xsp;
    if (jj_3R_34()) {
    jj_scanpos = xsp;
    if (jj_3R_35()) {
    jj_scanpos = xsp;
    if (jj_3_11()) {
    jj_scanpos = xsp;
    if (jj_3R_36()) {
    jj_scanpos = xsp;
    if (jj_3R_37()) return true;
    }
    }
    }
    }
    }
    }
    }
    return false;
  }

  private boolean jj_3R_31() {
    if (jj_3R_38()) return true;
    return false;
  }

  private boolean jj_3_4() {
    if (jj_3R_16()) return true;
    return false;
  }

  private boolean jj_3R_13() {
    if (jj_scan_token(ID)) return true;
    return false;
  }

  private boolean jj_3_2() {
    if (jj_3R_14()) return true;
    return false;
  }

  private boolean jj_3_10() {
    if (jj_scan_token(DOT)) return true;
    if (jj_scan_token(LENGTH)) return true;
    return false;
  }

  private boolean jj_3R_30() {
    if (jj_scan_token(INTEGER)) return true;
    return false;
  }

  private boolean jj_3_9() {
    if (jj_scan_token(DOT)) return true;
    if (jj_3R_13()) return true;
    return false;
  }

  private boolean jj_3R_17() {
    if (jj_3R_13()) return true;
    if (jj_scan_token(LSQPAREN)) return true;
    return false;
  }

  private boolean jj_3R_24() {
    if (jj_3R_28()) return true;
    return false;
  }

  private boolean jj_3R_14() {
    if (jj_3R_21()) return true;
    if (jj_3R_13()) return true;
    return false;
  }

  private boolean jj_3R_29() {
    if (jj_scan_token(BOOLEAN)) return true;
    return false;
  }

  private boolean jj_3R_15() {
    if (jj_scan_token(INTEGER)) return true;
    if (jj_scan_token(LSQPAREN)) return true;
    return false;
  }

  private boolean jj_3R_16() {
    if (jj_3R_13()) return true;
    if (jj_scan_token(ASSIGN)) return true;
    return false;
  }

  private boolean jj_3R_23() {
    if (jj_scan_token(NOT)) return true;
    return false;
  }

  private boolean jj_3R_20() {
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_23()) { jj_scanpos = xsp; break; }
    }
    if (jj_3R_24()) return true;
    return false;
  }

  private boolean jj_3R_27() {
    if (jj_3R_13()) return true;
    return false;
  }

  private boolean jj_3R_26() {
    if (jj_3R_30()) return true;
    return false;
  }

  private boolean jj_3R_25() {
    if (jj_3R_29()) return true;
    return false;
  }

  private boolean jj_3_3() {
    if (jj_3R_15()) return true;
    return false;
  }

  private boolean jj_3R_21() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_3()) {
    jj_scanpos = xsp;
    if (jj_3R_25()) {
    jj_scanpos = xsp;
    if (jj_3R_26()) {
    jj_scanpos = xsp;
    if (jj_3R_27()) return true;
    }
    }
    }
    return false;
  }

  private boolean jj_3_8() {
    if (jj_scan_token(TIMES)) return true;
    if (jj_3R_20()) return true;
    return false;
  }

  private boolean jj_3R_22() {
    if (jj_3R_20()) return true;
    return false;
  }

  private boolean jj_3_1() {
    if (jj_scan_token(CLASS)) return true;
    if (jj_3R_13()) return true;
    if (jj_scan_token(LBRACE)) return true;
    return false;
  }

  private boolean jj_3R_19() {
    if (jj_3R_22()) return true;
    return false;
  }

  private boolean jj_3R_38() {
    if (jj_scan_token(INTEGER_LITERAL)) return true;
    return false;
  }

  private boolean jj_3_7() {
    if (jj_scan_token(LTHAN)) return true;
    if (jj_3R_19()) return true;
    return false;
  }

  /** Generated Token Manager. */
  public MiniJavaParserTokenManager token_source;
  JavaCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  /** Whether we are looking ahead. */
  private boolean jj_lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[18];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x8000,0x8000,0x10000,0x400000,0x3000000,0x3000000,0x34001000,0x34001000,0x34001000,0x0,0x0,0x0,0x400,0x100,0x400,0x0,0x100,0x400000,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x1000,0x1000,0x0,0x1000,0x1000,0x6,0x6,0x200,0x100,0x1af0,0x0,0x1870,0x80,0x0,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[11];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public MiniJavaParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public MiniJavaParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new MiniJavaParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public MiniJavaParser(java.io.Reader stream) {
    jj_input_stream = new JavaCharStream(stream, 1, 1);
    token_source = new MiniJavaParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public MiniJavaParser(MiniJavaParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(MiniJavaParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = jj_lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List jj_expentries = new java.util.ArrayList();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      boolean exists = false;
      for (java.util.Iterator it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          exists = true;
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              exists = false;
              break;
            }
          }
          if (exists) break;
        }
      }
      if (!exists) jj_expentries.add(jj_expentry);
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[47];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 18; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 47; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 11; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
            case 6: jj_3_7(); break;
            case 7: jj_3_8(); break;
            case 8: jj_3_9(); break;
            case 9: jj_3_10(); break;
            case 10: jj_3_11(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
