/**
 * A list of objects.
 *
 * @author Joe Kiniry
 */

abstract class AbstractList /* extends Bag? implements Container? */
{
  /**
   * {@inheritDoc}
   * @question Which element?
   */
  abstract Object /* Comparable? */ getElement();

  /**
   * {@inheritDoc}
   * @question Where is the element added?
   * @question How does this method relate to {@link Container#addElement(Object)}?
   */
  abstract void addElement(Object /* or Comparable? */ o);

  /**
   * {@inheritDoc}
   */
  abstract int elementCount();

  /**
   * {@inheritDoc}
   */
  boolean isEmpty() {
    return (elementCount() == 0);
  }

  /**
   * Sort the objects in this list.
   */
  abstract void sort();
}
