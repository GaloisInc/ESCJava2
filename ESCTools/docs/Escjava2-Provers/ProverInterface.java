//@ model import org.jmlspecs.models.JMLObjectSequence;

interface ProverInterface
{
  //@ model boolean prover_started; in objectState;
  //@ model boolean prover_stopped; in objectState;

  //@ invariant prover_started ^ prover_stopped;

  //@ model boolean signature_defined; in objectState;

  //@ constraint \old(prover_stopped) ==> prover_stopped;

  //@ model JMLObjectSequence assumptions; in objectState;
  //@ invariant !assumptions.containsNull;
  //@ invariant assumptions.elementType == \type(Formula);

  /**
   * Start up the prover.  After the prover is started correctly it
   * should be ready to receive any of the commands embodied by all
   * the other methods of this API.
   *
   * @return a response code.  A response code of {@link
   * ProverResponse#OK} indicates that the prover started successfully
   * and is ready to receive commands.  A response code of {@link
   * ProverResponse#FAIL} indicates that the prover did not start
   * successfully and is not ready to receive commands.  In the latter
   * case, {@link ProverResponse.FAIL.info} can contain additional
   * arbitrary information about the failure.
   */
  /*@ public normal_behavior
    @   assignable objectState;
    @   ensures \result == ProverResponse.OK ||
    @           \result == ProverResponse.FAIL;
    @   ensures \result != ProverResponse.FAIL ==> prover_started;
    @   ensures \result == ProverResponse.FAIL ==> prover_stopped;
    @   ensures assumptions.isEmpty();
    @*/
  public /*@ non_null @*/ ProverResponse start_prover();

  /**
   * Send arbitrary information to the prover.  Typically this
   * information is not mandatory and is only suggestions or
   * informative information from the front-end.  This data is highly
   * prover-dependent.
   *
   * @param properties the set of property/value pairs to send to the
   * prover.
   * @return a response code.
   */
  /*@ public normal_behavior
    @   requires prover_started;
    @   assignable objectState;
    @   ensures \result == ProverResponse.OK || 
    @           \result == ProverResponse.FAIL ||
    @           \result == ProverResponse.SYNTAX_ERROR ||
    @           \result == ProverResponse.PROGRESS_INFORMATION;
    @   ensures \result != ProverResponse.FAIL ==> prover_started;
    @   ensures \result == ProverResponse.FAIL ==> prover_stopped;
    @*/
  public /*@ non_null @*/ ProverResponse set_prover_resource_flags(/*@ non_null @*/ Properties properties);

  /**
   * Send the signature of a new theory to the prover.
   *
   * Note that an empty signature is denoted by an empty {@link
   * Signature} object, <em>not</em> by a <code>null</code> value.
   *
   * @param signature the signature of the new theory.
   * @return a response code.
   */
  /*@ public normal_behavior
    @   requires prover_started;
    @   assignable objectState;
    @   ensures \result == ProverResponse.OK || 
    @           \result == ProverResponse.FAIL ||
    @           \result == ProverResponse.SYNTAX_ERROR;
    @   ensures \result != ProverResponse.FAIL ==> prover_started;
    @   ensures \result == ProverResponse.FAIL ==> prover_stopped;
    @   ensures signature_defined;
    @*/
  public /*@ non_null @*/ ProverResponse signature(/*@ non_null @*/ Signature signature);

  /**
   * Declare a new axiom in the current theory.
   *
   * @param formula
   * @return a response code.
   */
  /*@ public normal_behavior
    @   requires signature_defined;
    @   assignable objectState;
    @   ensures \result == ProverResponse.OK || 
    @           \result == ProverResponse.FAIL ||
    @           \result == ProverResponse.SYNTAX_ERROR ||
    @           \result == ProverResponse.INCONSISTENCY_WARNING;
    @   ensures \result != ProverResponse.FAIL ==> prover_started;
    @   ensures \result == ProverResponse.FAIL ==> prover_stopped;
    @*/
  public /*@ non_null @*/ ProverResponse declare_axiom(/*@ non_null @*/ Formula formula);

  /**
   * Make an assumption.
   *
   * @param formula the assumption to make.
   * @return a response code.
   */
  /*@ public normal_behavior
    @   requires signature_defined;
    @   assignable objectState;
    @   ensures \result == ProverResponse.OK || 
    @           \result == ProverResponse.FAIL ||
    @           \result == ProverResponse.SYNTAX_ERROR ||
    @           \result == ProverResponse.INCONSISTENCY_WARNING;
    @   ensures \result != ProverResponse.FAIL ==> prover_started;
    @   ensures \result == ProverResponse.FAIL ==> prover_stopped;
    @   ensures assumptions.equals(\old(assumptions.insertBack(formula)));
    @*/
  public /*@ non_null @*/ ProverResponse make_assumption(/*@ non_null @*/ Formula formula);

  /**
   * Retract some assumptions.
   *
   * @param count the number of assumptions to retract.
   * @return a response code.
   */
  /*@ public normal_behavior
    @   require count >= 0 || count == ALL;
    @   assignable objectState;
    @   ensures \result == ProverResponse.OK ||
    @           \result == ProverResponse.FAIL;
    @   ensures \result != ProverResponse.FAIL ==> prover_started;
    @   ensures \result == ProverResponse.FAIL ==> prover_stopped;
    @   ensures count != ALL && count <= assumptions.length() ==>
    @           assumptions.equals(\old(assumptions.prefix(assumptions.length() - count)));
    @   ensures count != ALL && assumptions.length() < count ==>
    @           assumptions.isEmpty();
    @   ensures count == ALL ==> assumptions.isEmpty();
    @*/
  public /*@ non_null @*/ ProverResponse retract_assumption(int count);

  /**
   * Check the validity of the given formula given the current theory,
   * its axioms, and the current set of assumptions.
   *
   * @param formula the formula to check.
   * @param properties a set of hints, primarily resource bounds on
   * this validity check.
   * @return a response code.
   *
   * @design kiniry 30 June 2004 - Possible alternative names for this
   * method include check(), is_entailed(), is_an_entailed_model_of().
   * I prefer is_valid().
   */
  /*@ public normal_behavior
    @   require count >= 0 || count == ALL;
    @   assignable objectState;
    @   ensures \result == ProverResponse.YES ||
    @           \result == ProverResponse.NO ||
    @           \result == ProverResponse.COUNTER_EXAMPLE ||
    @           \result == ProverResponse.SYNTAX_ERROR ||
    @           \result == ProverResponse.PROGRESS_INFORMATION ||
    @           \result == ProverResponse.TIMEOUT ||
    @           \result == ProverResponse.NO ||
    @           \result == ProverResponse.FAIL;
    @   ensures \result != ProverResponse.FAIL ==> prover_started;
    @   ensures \result == ProverResponse.FAIL ==> prover_stopped;
    @   ensures assumptions.equals(\old(assumptions));
    @*/
  public /*@ non_null @*/ ProverResponse is_valid(/*@ non_null @*/ Formula formula,
                                                  Properties properties);

  /**
   * Stop the prover.
   *
   * @return a response code.
   */
  /*@ public normal_behavior
    @   ensures \result == ProverResponse.OK ||
    @           \result == ProverResponse.FAIL;
    @   ensures prover_stopped;
    @   ensures assumptions.isEmpty();
    @*/
  public /*@ non_null @*/ ProverResponse stop_prover();
}
