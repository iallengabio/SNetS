package grmlsa.trafficGrooming;

import network.Circuit;
import network.ControlPlane;
import network.Node;
import request.RequestForConnection;
import util.Grooming;
import util.IntersectionFreeSpectrum;

import java.util.*;

/**
 * This class represents a Multihop Grooming with Min Spectrum Utilization policy.
 *
 *
 *
 * <p>
 * Created by Iallen on 10/09/2017.
 */
public class MGMSU extends MultihopGrooming {


    @Override
    protected double costFunction1(ArrayList<Circuit> sol, RequestForConnection rfc) {
        double res = 0;
        for(Circuit circuit : sol){
            circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth());

            res +=  (circuit.getModulation().requiredSlots(rfc.getRequiredBandwidth()) * circuit.getRoute().getHops());
        }
        return res;
    }


}