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
    protected double costFunction1(ArrayList<Circuit> sol) {
        throw new UnsupportedOperationException();
        //return 0;
    }

    @Override
    protected double costFunction2(ArrayList<Circuit> sol, RequestForConnection rfc) {
        throw new UnsupportedOperationException();
        //return 0;
    }

}