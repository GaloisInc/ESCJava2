package problems;

class FrameConditionsKey
{
  byte b;
  int i;
  Object o;
  String s;
  static Object so;
  
  //@ requires s != null;
  //@ assignable o;
  //@ ensures o != null;
  //@ signals (Exception) false;
  void l() {
    o = s;
  }

  //@ requires s != null;
  //@ assignable o;
  //@ ensures o.equals(s.toString());
  //@ signals (Exception) false;
  void m() {
    o = s.toString();
  }

  //@ requires Byte.MIN_VALUE <= i && i <= Byte.MAX_VALUE;
  //@ assignable b, s;
  //@ ensures s != null;
  //@ signals (Exception) false;
  void n() {
    b = (byte)i;
    if (b == i)
      s = new String();
    //@ assert s != null;
  }

  //@ requires o != null;
  //@ requires j < Byte.MIN_VALUE || Byte.MAX_VALUE < j;
  //@ assignable o;
  //@ ensures o != null;
  //@ signals (Exception) false;
  void o(int j) {
    if (j == b)
      o = null;
    //@ assert o != null;
  }
  
  //@ requires t != null;
  //@ requires t.length() >= 6;
  //@ assignable FrameConditionsKey.so;
  //@ ensures FrameConditionsKey.so.equals(t.substring(3,6));
  //@ signals (Exception) false;
  void p(String t) {
    FrameConditionsKey.so = t.substring(3,6);
  }

  //@ requires StaticFrameConditionsKey.s != null;
  //@ requires s == StaticFrameConditionsKey.s;
  //@ assignable StaticFrameConditionsKey.s;
  //@ ensures s == StaticFrameConditionsKey.s;
  //@ signals (Exception) false;
  void q() {
    if (StaticFrameConditionsKey.s.hashCode() >= Integer.MIN_VALUE) {
      StaticFrameConditionsKey.s = "foobar";
    }
    assert s == StaticFrameConditionsKey.s;
  }
  
  //@ requires true;
  //@ assignable i;
  //@ ensures i == 2;
  //@ signals (IllegalArgumentException) (\old(o == null));
  //@ signals (IllegalArgumentException iae) (\old(b < 0) && iae.getMessage().equals("bogus byte"));
  void r(Object o, byte b) throws IllegalArgumentException {
    if (o == null)
      throw new IllegalArgumentException();
    if (b < 0)
      throw new IllegalArgumentException("bogus byte");
    i = 2;
  }

  // assignable o, b, s, FrameConditionsKey.so, StaticFrameConditionsKey.s, i;
  void s() {
    //@ assume s != null;
    m();
    //@ assume Byte.MIN_VALUE <= i && i <= Byte.MAX_VALUE;
    n();
    //@ assume o != null;
    o(1000);
    p("foobar");
    //@ assume StaticFrameConditionsKey.s != null && s == StaticFrameConditionsKey.s;
    q();
    r("", (byte)1);
  }

  public static void main(String[] args) {
    FrameConditionsKey ppc = new FrameConditionsKey();
    ppc.s = "foobar"; // comment
    ppc.m();
    ppc.i = -1; // comment
    ppc.n();
    ppc.o = new Object(); // comment
    ppc.o(Byte.MAX_VALUE + 1); // comment
    ppc.p("foobar");
    StaticFrameConditionsKey.s = "piggie"; // comment
    ppc.s = StaticFrameConditionsKey.s; // comment
    ppc.q();
    ppc.r("", (byte)1);
  }
}

class StaticFrameConditionsKey {
  static int i;
  static String s;
}
