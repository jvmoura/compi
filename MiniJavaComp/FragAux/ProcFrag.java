package FragAux;

import Frame.*;
import Tree.*;

public class ProcFrag extends Frag {
    public Stm body;
    public Frame frame;

    public ProcFrag(Stm b, Frame f) {
        body = b;
        frame = f;
    }
}
