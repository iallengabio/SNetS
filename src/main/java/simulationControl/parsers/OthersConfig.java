package simulationControl.parsers;

import java.util.HashMap;
import java.util.Map;

public class OthersConfig {

    private Map<String, String> variables = new HashMap<>();

    public OthersConfig() {
    }

    public Map<String, String> getVariables() {
        return this.variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
