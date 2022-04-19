package dynNet.operationsImpl;

import dynNet.dynCalculator.Operation;

/**
 * Class [LogarithmOperation]
 * <p>
 * This is a concrete operation class, that implements the interface
 * <code>Operation</code>.
 *
 * @author Prof. Dr.-Ing. Wolf-Dieter Otte
 * @version May 20002
 */
public class LogarithmOperation implements Operation
{

    public float calculate(float firstNumber, float secondNumber)
    {
        // Math.log returns natural log for base e
        // For second operand as base, divide log firstnum base e, by
        // log secondnum base e.
        return (float) (Math.log(firstNumber) / Math.log(secondNumber));
    }
}
