
// This test corresponds to the bug reported in #955950
// It tests that P(o) in the code corresponds to P(o) in the annotation.
class M2 {
  M2();
  boolean b;
  String obj;

  /*@ pure @*/ boolean P(String o) {
    return b;
  }

  /*@ normal_behavior
        requires P(o);
        modifies \nothing;
        ensures !\result;
      also
      normal_behavior
        requires !P(o);
        modifies o.owner;
        ensures \result;
  */
  boolean m(/*@ non_null @*/ String o) {
    if (P(o)) {
      return false;
    }
    //@ set o.owner = this;
    return true;
  }
}
