package grmlsa.trafficGrooming;


import grmlsa.trafficGrooming.util.Grooming;
import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;

import java.util.Map;

public class AuxiliaryGraphGrooming_SSTG extends AuxiliaryGraphGrooming{
    private int sigmaExpansiveness=0;

    @Override
    protected void init(ControlPlane cp) {
        super.init(cp);
        Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
        this.sigmaExpansiveness = Integer.parseInt((String)uv.get("sigmaExpansiveness"));
    }

    @Override
    public void finishConnection(RequestForConnection rfc, ControlPlane cp) throws Exception {
        for (Circuit circuit : rfc.getCircuits()) {
            if (circuit.getRequests().size() == 1) {
                cp.releaseCircuit(circuit);
            } else {
                int numFinalSlots = circuit.getModulation().requiredSlots(circuit.getRequiredBandwidth() - rfc.getRequiredBandwidth());
                retractCircuit(circuit,numFinalSlots,cp);
                circuit.removeRequest(rfc);
            }
        }
    }

    protected void retractCircuit(Circuit circuit, int numFinalSlots,ControlPlane cp){
        int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
        int release = numCurrentSlots - numFinalSlots;

        int[] freeSlots = Grooming.circuitExpansiveness(circuit);
        int freeSlotsDown = freeSlots[0];
        int freeSlotsUp = freeSlots[1];
        float nfsd = sigmaExpansiveness - freeSlotsDown;
        float nfsu = sigmaExpansiveness - freeSlotsUp;

        int fu,fd;
        if(nfsd<0||(nfsd==nfsu&&nfsd==0)){
            fu=release;
            fd=0;
        }else{
            if(nfsu<0)nfsu=0;
            float tot = nfsd+nfsu;//normalize and convex
            nfsd = nfsd/tot;
            fd = Math.round(nfsd*release);
            fu = release-fd;
        }

        if (release > 0) {
            try {
                cp.retractCircuit(circuit, fd, fu);
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }
    }
}
