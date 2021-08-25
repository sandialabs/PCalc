package gov.sandia.gmp.bender;

import gov.sandia.gmp.util.exceptions.GMPException;

/**
 * A Bender exception that implies that the computed Ray is invalid for some
 * known reason.
 */
@SuppressWarnings("serial")
public class BenderException extends GMPException 
{
	public enum ErrorCode {SNELLS_LAW_TOPO, BRENTS_OUT_OF_PLANE, BENDING,
												 FATAL, NONFATAL};
	
	final private ErrorCode errorCode;
	
    public BenderException() 
    {
        super();
        errorCode = ErrorCode.FATAL;
    }

    public BenderException(ErrorCode errorCode, String string) 
    {
        super(string);
        this.errorCode = errorCode;
    }

    public BenderException(Throwable throwable) 
    {
        super(throwable.getCause());
        errorCode = ErrorCode.FATAL;
    }
    
    public BenderException(ErrorCode errorCode, Throwable throwable) 
    {
        super(throwable.getCause());
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode()
    {
    	return errorCode;
    }
    
//    public String toString()
//    {
//    	return String.format("BenderException %s  getMessage=%s%n",
//    			errorCode.toString(), getMessage());
//    }
}

