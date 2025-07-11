package syntaxtree;

import java.util.List;
import java.util.Vector;

public class ClassDeclList {
   private Vector list;

   public ClassDeclList() {
      list = new Vector();
   }

   public void addElement(ClassDecl n) {
      list.addElement(n);
   }

   public ClassDecl elementAt(int i)  { 
      return (ClassDecl)list.elementAt(i); 
   }

   public int size() { 
      return list.size(); 
   }

   public List<ClassDecl> getList() {
		return list;
	}
}
