package grmlsa.trafficGrooming;

import network.ControlPlane;
import request.RequestForConnection;

import java.util.Map;

/**
 * This class represents a Multihop Traffic Grooming based on Solution Ranking (MTGSR). This algorithm selects the traffic grooming solution for each request based at a composite cost function.
 * The parameters alfa, beta, gama, delta and epsilon has to be seted at 'others' config file.
 */
public class MTGSR extends MultihopGrooming {
    private Double alfa;
    private Double beta;
    private Double gama;
    private Double delta;
    private Double epsilon;
    private Double fi;
    @Override
    protected double costFunction(MultihopSolution sol, RequestForConnection rfc, ControlPlane cp) {
        if (this.alfa == null) {
            Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
            if(uv.get("alfa")!=null) this.alfa = Double.parseDouble((String)uv.get("alfa"));
            else this.alfa = 0.0;
            if(uv.get("beta")!=null) this.beta = Double.parseDouble((String)uv.get("beta"));
            else this.beta = 0.0;
            if(uv.get("gama")!=null) this.gama = Double.parseDouble((String)uv.get("gama"));
            else this.gama = 0.0;
            if(uv.get("delta")!=null) this.delta = Double.parseDouble((String)uv.get("delta"));
            else this.delta = 0.0;
            if(uv.get("epsilon")!=null) this.epsilon = Double.parseDouble((String)uv.get("epsilon"));
            else this.epsilon = 0.0;
            if(uv.get("fi")!=null) this.fi = Double.parseDouble((String)uv.get("fi"));
            else this.fi = 0.0;
        }


        MultihopSolutionStatistics mss = sol.statistics;
        return this.alfa * mss.physicalHops + this.beta * mss.virtualHops + this.gama * mss.spectrumUtilization + this.delta * (double)mss.transceivers - this.epsilon * mss.meanSNR + this.fi * mss.SNRImpact;
    }
}
