(* Copyright (C) 1992, Digital Equipment Corporation *)
(* All rights reserved. *)
(* See the file COPYRIGHT for a full description. *)
(* *)
(* by Steve Glassman, Mark Manasse and Greg Nelson *)
(* Last modified on Mon Feb 24 13:59:48 PST 1992 by muller *)
(* modified on Fri Sep 13 5:05:45 PDT 1991 by msm *)
<*PRAGMA LL*>

UNSAFE MODULE XEventQueue;

IMPORT X;

CONST NormalQSize = 8;

<*INLINE*> PROCEDURE IsEmpty (READONLY rb: T): BOOLEAN =
  BEGIN
    RETURN rb.lo = rb.hi
  END IsEmpty;

PROCEDURE Insert (VAR rb: T; READONLY e: X.XEvent) =
  BEGIN
    IF rb.buff = NIL THEN
      rb.buff := NEW(REF ARRAY OF X.XEvent, NormalQSize)
    END;
    rb.buff[rb.hi] := e;
    INC(rb.hi);
    WITH n = NUMBER(rb.buff^) DO
      IF rb.hi = n THEN rb.hi := 0 END;
      IF rb.hi = rb.lo THEN
        WITH new = NEW(REF ARRAY OF X.XEvent, 2 * n) DO
          FOR i := rb.lo TO n - 1 DO new[i] := rb.buff[i] END;
          FOR i := 0 TO rb.hi - 1 DO new[i + n] := rb.buff[i] END;
          INC(rb.hi, n);
          rb.buff := new
        END
      END
    END
  END Insert;

PROCEDURE Remove (VAR rb: T): X.XEvent RAISES {Exhausted} =
  VAR res: X.XEvent;
  BEGIN
    IF rb.lo = rb.hi THEN RAISE Exhausted END;
    res := rb.buff[rb.lo];
    WITH n = NUMBER(rb.buff^) DO
      INC(rb.lo);
      IF rb.lo = n THEN rb.lo := 0 END;
      IF (rb.lo = rb.hi) AND (n > NormalQSize) THEN rb := Empty END
    END;
    RETURN res
  END Remove;

PROCEDURE Peek (VAR rb: T): X.XEvent RAISES {Exhausted} =
  BEGIN
    IF rb.lo = rb.hi THEN RAISE Exhausted END;
    RETURN rb.buff[rb.lo];
  END Peek;

BEGIN
END XEventQueue.
