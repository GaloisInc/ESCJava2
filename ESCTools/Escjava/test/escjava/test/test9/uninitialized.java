
public class uninitialized {
  int m1(boolean b) {
    int t =0 /*@ uninitialized */;
    int f =0/*@ uninitialized */;
    if (b) t = 0;
    else f = 1;
    int result=0 /*@ uninitialized */;
    if (! b) result = f;
    else result = t;
    //@ assert b ==> result == 0;
    //@ assert !b ==> result == 1;
    return result;
  }
}
