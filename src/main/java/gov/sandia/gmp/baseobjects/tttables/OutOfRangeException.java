/**
 * 
 */
package gov.sandia.gmp.baseobjects.tttables;

/**
 * @author sballar
 *
 */
@SuppressWarnings("serial")
public class OutOfRangeException extends Exception {

	public OutOfRangeException() {
		super();
	}

	public OutOfRangeException(String string) {
		super(string);
	}

	public OutOfRangeException(String string, Throwable throwable) {
		super(string, throwable);
	}

	public OutOfRangeException(Throwable throwable) {
		super(throwable);
	}

}
