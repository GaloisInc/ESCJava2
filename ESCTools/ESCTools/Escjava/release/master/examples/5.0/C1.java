class C1 {

  int n;

  //@ requires a != null | b != null;
  static int m(C1 a, C1 b) {
    if (a != null) {
      return a.n;
    } else {
      return b.n;
    }
  }
}
