package FragAux;

public class Frag {
    private Frag next;

    public Frag() {
        next = null;
    }

    public Frag(Frag next) {
        this.next = next;
    }

    public void addNext(Frag next) {
        this.next = next;
    }

    public Frag getNext() {
        return next;
    }

    public boolean hasNext() {
        return this.next != null;
    }
}
