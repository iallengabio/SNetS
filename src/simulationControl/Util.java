package simulationControl;

import java.util.HashSet;
import java.util.Set;

import network.Pair;

/**
 * Esta classe deverá gravar os pares, larguras de banda e demais valores necessários para a gravação dos resultados da simulação em arquivo
 * @author Iallen
 *
 */
public class Util {
	
	public static Set<Double> bandwidths = new HashSet<>();
	
	public static Set<Pair> pairs = new HashSet<>();
	
	public static String projectPath = "";
	
	
	public static void reset(){
		bandwidths = new HashSet<>();
		pairs = new HashSet<Pair>();
	}
}
