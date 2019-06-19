package grmlsa.trafficGrooming;

import network.ControlPlane;
import request.RequestForConnection;

import java.util.Map;

/**
 * This class implements the mechanism Spectrum Reservation for each Node Pair in MTGSR algorithm.
 */
public class MTGSR_SRNP extends MultihopGroomingSRNP {
    private Double alfa;
    private Double beta;
    private Double gama;
    private Double delta;
    private Double epsilon;
    @Override
    protected double costFunction(MultihopSolution sol, RequestForConnection rfc, ControlPlane cp) {
        if (this.alfa == null) {
            Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
            this.alfa = Double.parseDouble((String)uv.get("alfa"));
            this.beta = Double.parseDouble((String)uv.get("beta"));
            this.gama = Double.parseDouble((String)uv.get("gama"));
            this.delta = Double.parseDouble((String)uv.get("delta"));
            this.epsilon = Double.parseDouble((String)uv.get("epsilon"));
        }

        MultihopSolutionStatistics mss = sol.statistics;
        return this.alfa * mss.physicalHops + this.beta * mss.virtualHops + this.gama * mss.spectrumUtilization + this.delta * (double)mss.transceivers - this.epsilon * mss.meanSNR;
    }
}
