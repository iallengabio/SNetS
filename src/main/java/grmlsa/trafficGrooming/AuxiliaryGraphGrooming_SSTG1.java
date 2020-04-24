package grmlsa.trafficGrooming;


import grmlsa.trafficGrooming.util.Grooming;
import network.Circuit;
import network.ControlPlane;
import request.RequestForConnection;

import java.util.Map;

public class AuxiliaryGraphGrooming_SSTG1 extends AuxiliaryGraphGrooming{
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

    @Override
    protected int[] decideToExpand(int numMoreSlots, int numLowerFreeSlots, int numUpperFreeSlots) {
        int eu=0, ed=0;

        int dfsd = numLowerFreeSlots - sigmaExpansiveness;
        int dfsu = numUpperFreeSlots - sigmaExpansiveness;

        int fsd = numLowerFreeSlots;
        int fsu = numUpperFreeSlots;

        if(dfsd>0){//mais slots livres abaixo do que sigma
            int aux = Math.min(numMoreSlots,dfsd);
            ed+=aux;
            numMoreSlots-=aux;
            fsd-=aux;
        }

        if(numMoreSlots>0 && dfsu>0){//mais slots livres acima do que sigma
            int aux = Math.min(numMoreSlots,dfsu);
            eu+=aux;
            numMoreSlots-=aux;
            fsu-=aux;
        }

        if(numMoreSlots>0){
            if(fsd>fsu){
                int aux = Math.min(fsd-fsu,numMoreSlots);
                ed+=aux;
                numMoreSlots-=aux;
                fsd-=aux;
            }else{
                int aux = Math.min(fsu-fsd,numMoreSlots);
                eu+=aux;
                numMoreSlots-=aux;
                fsu-=aux;
            }
        }

        if(numMoreSlots>0){
            int aux = numMoreSlots/2;
            ed+=aux;
            eu+=(numMoreSlots-aux);
        }

        int res[] = new int[2];
        res[0] = ed;
        res[1] = eu;
        return res;
    }

    protected void retractCircuit(Circuit circuit, int numFinalSlots, ControlPlane cp){
        int numCurrentSlots = circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0] + 1;
        int release = numCurrentSlots - numFinalSlots;

        int[] freeSlots = Grooming.circuitExpansiveness(circuit);
        int freeSlotsDown = freeSlots[0];
        int freeSlotsUp = freeSlots[1];
        int dd = freeSlotsDown - sigmaExpansiveness;
        int du = freeSlotsUp - sigmaExpansiveness;
        int rd = 0, ru = 0;

        if(dd>=0){
            ru=release;
            rd=0;
        }else{
            if(freeSlotsDown<freeSlotsUp){
                int aux = Math.min(Math.min(dd*-1,freeSlotsUp-freeSlotsDown),release);
                rd+=aux;
                release-=aux;
            }else{
                int aux = Math.min(Math.min(du*-1,freeSlotsDown-freeSlotsUp),release);
                ru+=aux;
                release-=aux;
            }

            if(release>0){//ainda há slots para liberar
                int aux = -1*(dd+rd);
                aux = Math.min(aux,release-release/2);
                rd+=aux;
                release-=aux;
                ru+=release;
            }
        }

        try {
            cp.retractCircuit(circuit, rd, ru);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        }




    }
}
