package org.scijava.event.bushe;

/**
 * @author Michael Bushe
 * @since Nov 19, 2005 11:00:42 PM
 */
public class VetoEventListenerForTest implements VetoEventListener {
   private boolean throwException;

   public VetoEventListenerForTest() {
      this(false);
   }

   public VetoEventListenerForTest(boolean throwException) {
      this.throwException = throwException;
   }

   public boolean shouldVeto(Object evt) {
      if (throwException) {
         throw new IllegalArgumentException("veto ex");
      }
      return true;
   }
}
