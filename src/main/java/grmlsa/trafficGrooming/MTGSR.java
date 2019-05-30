package grmlsa.trafficGrooming;

import network.ControlPlane;
import request.RequestForConnection;

import java.util.Map;

/**
 * This class represents a Multihop Traffic Grooming based on Solution Ranking (MTGSR). This algorithm selects the traffic grooming solution for each request based at a composite cost function.
 * The parameters alfa, beta, gama, delta and epsilon has to be seted at 'others' config file.
 */
public class MTGSR extends MultihopGrooming2 {
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
        double res = this.alfa * mss.physicalHops + this.beta * mss.virtualHops + this.gama * mss.spectrumUtilization + this.delta * (double)mss.transceivers - this.epsilon * mss.meanSNR;
        return res;
    }
}
