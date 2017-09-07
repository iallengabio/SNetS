package util.tools.artificialBeeColony;

import java.util.ArrayList;
import java.util.List;

public interface ABCInterface {
	
	public boolean execute();
	public List<Double> getIGDepoch();
	public ArrayList<FoodSource> getArchive();
}