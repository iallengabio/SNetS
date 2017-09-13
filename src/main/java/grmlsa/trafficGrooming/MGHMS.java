package grmlsa.trafficGrooming;

import network.Circuit;
import request.RequestForConnection;

import java.util.ArrayList;

/**
 * This class represents a Multihop Grooming with Highest Minimum SNR.
 *
 *
 *
 * <p>
 * Created by Iallen on 10/09/2017.
 */
public class MGHMS extends MultihopGrooming {


    @Override
    protected double costFunction1(ArrayList<Circuit> sol, RequestForConnection rfc) {
        double minSNR = 1000000000;
        for(Circuit circuit : sol){
            double snr = circuit.getSNR();
            if(snr < minSNR) minSNR = snr;
        }
        return 1/minSNR; //because we want the grooming solution with the highest minimum snr to be chosen
    }


}