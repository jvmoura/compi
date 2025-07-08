
class Teste {
  public static void main(String[] a) {
    System.out.println(new Test().foo(3));
  }
}

class Test {
  public int foo(int x) {
    int z;
    z = x + 1;
    z = z - 1;
    return z;
  }
}
