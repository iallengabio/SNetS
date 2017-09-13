package grmlsa.trafficGrooming;

import network.Circuit;
import request.RequestForConnection;

import java.util.ArrayList;

/**
 * This class represents a Multihop Grooming with Highest Minimum Delta SNR.
 *
 *
 *
 * <p>
 * Created by Iallen on 10/09/2017.
 */
public class MGHMDS extends MultihopGrooming {


    @Override
    protected double costFunction1(ArrayList<Circuit> sol, RequestForConnection rfc) {
        double minDeltaSNR = 1000000000;
        for(Circuit circuit : sol){
            double snr = circuit.getSNR();
            double snRthreshold = circuit.getModulation().getSNRthreshold();
            double deltaSNR = snr - snRthreshold;
            if(deltaSNR < minDeltaSNR) minDeltaSNR = deltaSNR;
        }
        return 1/minDeltaSNR; //because we want the grooming solution with the highest minimum delta snr to be chosen
    }


}