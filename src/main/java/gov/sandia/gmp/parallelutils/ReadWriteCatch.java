package gov.sandia.gmp.parallelutils;

import java.io.IOException;

public interface ReadWriteCatch
{
  public void readCatch(String fp) throws IOException;
  public void writeCatch(String fp) throws IOException;
  public String catchExceptionString(int ecnt, Exception ex, String fp);
}
