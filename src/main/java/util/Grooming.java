package util;

import network.Circuit;

import java.util.List;

/**
 * This class provides some useful methods for traffic grooming algorithms
 * Created by Iallen on 11/08/2017.
 */
public class Grooming {

    /**
     * This method analyzes the available spectrum frequencies and calculates the potential for expansion of a circuit.
     * @param circuit
     * @return A vector of integers with two positions. The first position is the lower expansion potential of the circuit. The second position is the upper expansion potential of the circuit.
     */
    public static int[] circuitExpansiveness(Circuit circuit){
        int res[] = new int[2];
        List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());
        int[] bandFreeAdjInferior = IntersectionFreeSpectrum.bandAdjacentInferior(circuit.getSpectrumAssigned(), composition);
        res[0] = 0;
        if(bandFreeAdjInferior != null){
            res[0] = bandFreeAdjInferior[1] - bandFreeAdjInferior[0] + 1;
        }
        int[] bandFreeAdjSuperior = IntersectionFreeSpectrum.bandAdjacentSuperior(circuit.getSpectrumAssigned(), composition);
        res[1] = 0;
        if(bandFreeAdjSuperior != null){
            res[1] = bandFreeAdjSuperior[1] - bandFreeAdjSuperior[0] + 1;
        }
        return res;
    }
}
