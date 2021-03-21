package grmlsa.spectrumAssignment;

import network.Circuit;
import network.ControlPlane;
import network.Link;
import util.ComputesFragmentation;
import util.IntersectionFreeSpectrum;

import java.util.List;
import java.util.Map;

public class MultifactorSpectrumAssignment implements SpectrumAssignmentAlgorithmInterface{
    //weights
    private Double a; //for slots usage
    private Double b; //for average fragmentation
    private Double c; //for expandability
    private Double d; //for delta SNR
    private Double e; //for SNR impact
    boolean init = true;

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
        if(init){
            this.init(cp);
        }
        List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand());
        int chosen[] = policy(numberOfSlots, composition, circuit, cp);
        circuit.setSpectrumAssigned(chosen);
        if (chosen == null)
            return false;

        return true;
    }

    public double getCost(Circuit circuit, ControlPlane cp){
        double qs, fm, ex, dsnr, snri;

        int sa[] = circuit.getSpectrumAssigned();
        qs = circuit.getRoute().getHops() * (sa[1]-sa[0]+1);

        fm = 0;
        ComputesFragmentation cf = new ComputesFragmentation();
        for (Link l : circuit.getRoute().getLinkList()) {
            fm += cf.externalFragmentation(l.getFreeSpectrumBands(circuit.getGuardBand()));
        }
        fm /= circuit.getRoute().getHops();

        List<int[]> merge = IntersectionFreeSpectrum.merge(circuit.getRoute(),circuit.getGuardBand());
        ex = 0;
        ex += IntersectionFreeSpectrum.freeSlotsDown(circuit.getSpectrumAssigned(),merge,circuit.getGuardBand());
        ex += IntersectionFreeSpectrum.freeSlotsUpper(circuit.getSpectrumAssigned(),merge,circuit.getGuardBand());

        dsnr = cp.getDeltaSNR(circuit);

        snri = cp.computesImpactOnSNROther(circuit);

        return this.costFunction(qs,fm,ex,dsnr,snri);
    }

    private double testSpectrumRange(int chosen[], double qs, double ex, ControlPlane cp, Circuit circuit){
        double fm, dsnr, snri;
        double cost = Double.MAX_VALUE;
        try {
            if(cp.isAdmissibleQualityOfTransmission(circuit)) {
                cp.allocateCircuit(circuit);
                fm = 0;
                ComputesFragmentation cf = new ComputesFragmentation();
                for (Link l : circuit.getRoute().getLinkList()) {
                    fm += cf.externalFragmentation(l.getFreeSpectrumBands(circuit.getGuardBand()));
                }
                fm /= circuit.getRoute().getHops();

                dsnr = cp.getDeltaSNR(circuit);

                snri = cp.computesImpactOnSNROther(circuit);

                cost = this.costFunction(qs,fm,ex,dsnr,snri);

                cp.releaseCircuit(circuit);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return cost;
    }

    @Override
    public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
        int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
        if(numberOfSlots> maxAmplitude) return null;

        int chosen[] = null;

        double qs, fm, ex, dsnr, snri;
        qs = circuit.getRoute().getHops() * numberOfSlots;
        double bestCost = Double.MAX_VALUE;
        int realyChosen[] = null;
        for (int[] band : freeSpectrumBands) {

            if (band[1] - band[0] + 1 >= numberOfSlots) {
                chosen = band.clone();
                chosen[1] = chosen[0] + numberOfSlots - 1;//FF
                circuit.setSpectrumAssigned(chosen);
                ex = band[1] - band[0] + 1 - numberOfSlots;

                double cost = this.testSpectrumRange(chosen,qs,ex,cp,circuit);
                if(cost<bestCost){
                    realyChosen = chosen;
                    bestCost = cost;
                }

                chosen = band.clone();
                chosen[0] = chosen[1] - numberOfSlots + 1;//LF
                circuit.setSpectrumAssigned(chosen);
                ex = band[1] - band[0] + 1 - numberOfSlots;

                cost = this.testSpectrumRange(chosen,qs,ex,cp,circuit);
                if(cost<bestCost){
                    realyChosen = chosen;
                    bestCost = cost;
                }

                chosen = band.clone();
                int sl = chosen[1] - chosen[0] + 1;
                sl -= numberOfSlots;
                sl /= 2;
                chosen[0] = chosen[0] + sl;
                chosen[1] = chosen[0] + numberOfSlots - 1;//MF
                circuit.setSpectrumAssigned(chosen);
                ex = band[1] - band[0] + 1 - numberOfSlots;

                cost = this.testSpectrumRange(chosen,qs,ex,cp,circuit);
                if(cost<bestCost){
                    realyChosen = chosen;
                    bestCost = cost;
                }
            }
        }

        return realyChosen;
    }

    private double costFunction(double qs, double fm, double ex, double dsnr, double snri) {
        return a*qs + b*fm - c*ex - d*dsnr + e*snri;
    }

    private void init(ControlPlane cp){
        Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
        a = Double.parseDouble((String)uv.get("mfrsa_a"));
        b = Double.parseDouble((String)uv.get("mfrsa_b"));
        c = Double.parseDouble((String)uv.get("mfrsa_c"));
        d = Double.parseDouble((String)uv.get("mfrsa_d"));
        e = Double.parseDouble((String)uv.get("mfrsa_e"));
        init = false;
    }
}
