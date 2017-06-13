package simulationControl.resultManagers;

import java.io.IOException;
import java.util.List;

import measurement.Measurement;

/**
 * Interface that must be implemented by the classes that generate the result files of the performance metrics.
 * 
 * @author Alexandre
 */
public interface ResultManagerInterface {
	
	/**
	 * This method returns a formatted string with the results generated by performance metrics.
	 * 
	 * @param llms List<List<Measurement>>
	 * @throws IOException
	 */
	public String result(List<List<Measurement>> llms) throws IOException;
}
