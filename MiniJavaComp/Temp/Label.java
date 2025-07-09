package Temp;

import Symbol.Symbol;

public class Label {
    private String name;
    private static int count = 0;

    /* Gera um label com nome arbitrário */
    public Label() {
        this("L" + count++);
    }

    /* Gera label com base em uma string */
    public Label(String s) {
        name = s;
    }

    /* Gera label com base em um Symbol */
    public Label(Symbol s) {
        name = s.toString();
    }

    public String toString() {
        return name;
    }
}
