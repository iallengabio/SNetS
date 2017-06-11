package simulationControl.resultManagers;

import java.io.IOException;
import java.util.List;

import measurement.Measurement;

public interface IResultManager {
	public void result(String path, String fileName, List<List<Measurement>> llms) throws IOException;
}
