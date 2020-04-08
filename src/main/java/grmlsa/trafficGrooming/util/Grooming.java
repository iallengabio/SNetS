package grmlsa.trafficGrooming.util;

import network.Circuit;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

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
        res[0] = IntersectionFreeSpectrum.freeSlotsDown(circuit.getSpectrumAssigned(),composition);
        res[1] = IntersectionFreeSpectrum.freeSlotsUpper(circuit.getSpectrumAssigned(),composition);
        return res;
    }

    public static boolean canBeExpanded(Circuit circuit, RequestForConnection rfc) {
        int[] exp = Grooming.circuitExpansiveness(circuit);
        int circExCap = exp[0] + exp[1];
        int slotsNeeded = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() + rfc.getRequiredBandwidth()) - (circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1);
        return circExCap >= slotsNeeded;
    }
}
