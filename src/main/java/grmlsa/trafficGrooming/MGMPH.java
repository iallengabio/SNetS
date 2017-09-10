package grmlsa.trafficGrooming;

import network.Circuit;
import network.ControlPlane;
import network.Node;
import request.RequestForConnection;
import util.Grooming;
import util.IntersectionFreeSpectrum;

import java.util.*;

/**
 * This class represents a Multihop Grooming with Min Physical Hops policy.
 *
 *
 *
 * <p>
 * Created by Iallen on 10/08/2017.
 */
public class MGMPH extends MultihopGrooming {

    /**
     * This cost function is used to compare simple eletric grooming solutions.
     * @param sol
     * @return
     */
    protected double costFunction1(ArrayList<Circuit> sol, RequestForConnection rfc) {
        double res = 0;
        for(Circuit circuit : sol){
            res += circuit.getRoute().getHops();
        }
        return res;
    }

}