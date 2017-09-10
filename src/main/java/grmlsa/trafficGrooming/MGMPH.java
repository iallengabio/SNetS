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
    protected double costFunction1(ArrayList<Circuit> sol) {
        double res = 0;
        for(Circuit circuit : sol){
            res += circuit.getRoute().getHops();
        }
        return res;
    }

    /**
     * This cost function is used to compare solutions of grooming that need to expand some circuits.
     * @param sol
     * @param rfc
     * @return
     */
    protected double costFunction2(ArrayList<Circuit> sol, RequestForConnection rfc){
        double res = 0;
        for (Circuit circuit : sol) {
            if (circuit.getResidualCapacity() < rfc.getRequiredBandwidth()) {
                res++;
            }
        }

        res = res * 100 + costFunction1(sol); //In case of a tie, preference should be given to solutions with fewer virtual hops.

        return res;
    }

}