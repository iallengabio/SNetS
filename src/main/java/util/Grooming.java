package util;

import java.util.List;

import network.Circuit;

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
        List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand());
        res[0] = IntersectionFreeSpectrum.freeSlotsDown(circuit.getSpectrumAssigned(), composition, circuit.getGuardBand());
        res[1] = IntersectionFreeSpectrum.freeSlotsUpper(circuit.getSpectrumAssigned(), composition, circuit.getGuardBand());
        return res;
    }
}
