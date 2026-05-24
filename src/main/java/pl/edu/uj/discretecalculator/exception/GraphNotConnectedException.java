//wyjątek jeśli potrzeba spójności
package pl.edu.uj.discretecalculator.exception;
import pl.edu.uj.discretecalculator.exception.GraphException;

public class GraphNotConnectedException extends GraphException
{
    public GraphNotConnectedException(String s)
    {
        super(s);
    }
}
