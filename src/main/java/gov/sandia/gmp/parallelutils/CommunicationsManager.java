package gov.sandia.gmp.parallelutils;

import java.io.IOException;

/**
 * Provides a means for remote network components to communicate with each
 * other.
 * @author bjlawry
 */
public interface CommunicationsManager{
	/**
	 * Sends the specified message to the client.
	 * @param message
	 * @throws IOException
	 */
	public void sendToClient(Object message) throws IOException;
}
