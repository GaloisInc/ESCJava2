// Tests the handling of special modifies clauses

public class ModE extends ModES {

	public int i;

	//@ modifies \everything;
	public ModE();

	//@ modifies \not_specified;
	public ModE(int i);

	//@ modifies \nothing;
	public ModE(float f);

	//@ modifies i;
	public ModE(int jj, int j);

	public ModE(int i, int j, int k);

	//@ modifies \everything;
	public void me();

	//@ modifies \nothing;
	public void mn();

	//@ modifies \not_specified;
	public void mns();

	public void md();

	//@ modifies i;
	public void mi();

	//@ modifies \nothing;
	public void mm() {
				// These give cautions for now
		ModE e = new ModE();
		e = new ModE(5);
		e.me();
		e.mns();
		e = new ModE(1,2,3);
		e.md();
		e.mss();
				// These are OK
		e.mi();
		e.mn();
		e = new ModE(1,2);
		e = new ModE((float)5.0);
		e.ms();
	}
}

class ModES extends ModESS {

	public void ms();

	//@ also ensures true;
	public void mss();

}

class ModESS {

	//@ modifies \nothing;
	public void ms();

	//@ modifies \nothing;
	public void mss();
}
