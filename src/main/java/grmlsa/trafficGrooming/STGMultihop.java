package grmlsa.trafficGrooming;

import network.ControlPlane;
import request.RequestForConnection;

/**
 * This class represents a Simple Traffic Grooming Multihop Algorithm.
 *
 * Created by Iallen on 10/08/2017.
 */
public class STGMultihop implements TrafficGroomingAlgorithmInterface {
    @Override
    public boolean searchCircuitsForGrooming(RequestForConnection rfc, ControlPlane cp) {
        return false;
    }

    @Override
    public void finishConnection(RequestForConnection rfc, ControlPlane cp) {

    }
}
