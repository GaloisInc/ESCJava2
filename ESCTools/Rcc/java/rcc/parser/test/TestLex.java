/* Copyright 2000, 2001, Compaq Computer Corporation */


package escjava.parser.test;

import escjava.ast.TagConstants;
import escjava.ast.EscPrettyPrint;
import escjava.parser.EscPragmaParser;

import javafe.ast.Identifier;
import javafe.ast.ASTNode;
import javafe.ast.LexicalPragma;
import javafe.ast.LexicalPragmaVec;
import javafe.ast.StmtPragma;
import javafe.ast.TypeDeclElemPragma;
import javafe.ast.ModifierPragma;
import javafe.ast.PrettyPrint;
import javafe.ast.StandardPrettyPrint;
import javafe.parser.Lex;
import javafe.util.Assert;
import javafe.util.CorrelatedReader;
import javafe.util.Location;

import java.io.IOException;
import java.io.FileInputStream;
import java.util.Random;

public class TestLex {
  public static void main(String[] argv) throws IOException {
    EscPrettyPrint p = new EscPrettyPrint();
    p.del = new StandardPrettyPrint(p);
    PrettyPrint.inst = p;

    boolean lookahead = false;
    int argi = 0;
    for( ; argi < argv.length; argi++) {
      if (argv[argi].equals("lookahead")) lookahead = true;
      else break;
    }
    if (argi + 1 != argv.length) {
      System.out.println("Usage: java escjava.parser.test.TestLex [lookahead] <file>");
      System.exit(2);
    }

    // BUGBUG:  I don't understand what the following loop is supposed to
    // do, but I fear it doesn't do what it's supposed to do.  --KRML
    for(int i = 0; i < esckeywords.length; i++) {
     int t = i + escjava.ast.TagConstants.FIRSTESCKEYWORDTAG;
     Assert.notFalse(!esckeywords.equals(escjava.ast.TagConstants.toString(t)),
		     "Ouch on " + esckeywords[i] + " != "
		     + escjava.ast.TagConstants.toString(t));
    }

    Lex ll = new Lex(new EscPragmaParser(), false);
    ll.restart(new CorrelatedReader(new FileInputStream(argv[argi]),
				    argv[argi]));

    Random r = new Random(System.currentTimeMillis());

    int lac = (lookahead ? Math.abs(r.nextInt()) % 9 + 1 : 0);
    int la = ll.lookahead(lac);
    while (ll.ttype != TagConstants.EOF) {
      Assert.notFalse(lac != 0 || ll.ttype == la,
		      "Bad lookahead, is " + TagConstants.toString(ll.ttype)
		      + ", expected " + TagConstants.toString(la));

      // Print token
      System.out.print(Location.toFileName(ll.startingLoc));
      if (! Location.isWholeFileLoc(ll.startingLoc))
	System.out.print(":" + Location.toLineNumber(ll.startingLoc));
      System.out.print(": ");
      int tag = ((ASTNode)ll.auxVal).getTag();
      if (ll.auxVal instanceof TypeDeclElemPragma)
	PrettyPrint.inst.print(System.out, 0, (TypeDeclElemPragma)ll.auxVal);
      else if (ll.auxVal instanceof ModifierPragma)
	PrettyPrint.inst.print(System.out, 0, (ModifierPragma)ll.auxVal);
      else if (ll.auxVal instanceof StmtPragma)
	PrettyPrint.inst.print(System.out, 0, (StmtPragma)ll.auxVal);
      else 
	Assert.notFalse(false, "Unexpected tag " + TagConstants.toString(tag));
      System.out.println("");

      ll.getNextToken();
      if (lac == 0) {
	lac = (lookahead ? Math.abs(r.nextInt()) % 9 + 1 : 0);
	la = ll.lookahead(lac);
      } else lac--;
      ll.zzz();
    }

    // Print lexical pragmas
    LexicalPragmaVec lpv = ll.getLexicalPragmas();
    for(int i = 0; i < lpv.size(); i++) {
      LexicalPragma lp = lpv.elementAt(i);
      System.out.print(Location.toFileName(lp.getStartLoc()));
      if (! Location.isWholeFileLoc(lp.getStartLoc()))
	System.out.print(":" + Location.toLineNumber(lp.getStartLoc()));
      System.out.print(": ");
      PrettyPrint.inst.print(System.out, lp);
    }

    Identifier.check();
    TagConstants.zzzz();
    ll.zzz();
  }

  private static String[] esckeywords = {
    "also_ensures", "also_modifies", "assume", "assert",
    "axiom", "defined_if", "ensures", "elemtype",
    "exists", "fresh", "forall", "invariant",
    "LBLPOS", "LBLNEG", "loop_invariant", "LS",
    "min", "modifies", "monitored", "monitored_by",
    "nowarn", "PRE", "RES", "requires", "still_deferred", "type",
    "typeof", "uninitialized", "unreachable", "writeable_deferred"
  };
}

