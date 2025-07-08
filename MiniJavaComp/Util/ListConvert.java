package Util;

import java.util.List;

import Tree.ExpList;

public class ListConvert <T> {
    public ExpList listToExpList(List<T> list) {
        return listToExpList_(list, 0);
    }

    public ExpList listToExpList_(List<T> list, int i) {
        if (i == list.size() - 1) {
            return new ExpList(list.get(i), null);
        } else {
            return new ExpList(list.get(i), listToExpList_(list, i+=1));
        }
    }

    public void Print(ExpList list) {
        Print_(list);
    }

    public void Print_(ExpList list) {
        System.out.println(list.head);
        if (list.tail != null) {
            Print_(list.tail);
        }
    }
}
