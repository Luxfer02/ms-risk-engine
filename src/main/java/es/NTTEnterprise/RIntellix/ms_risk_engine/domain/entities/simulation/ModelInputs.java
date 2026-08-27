package es.NTTEnterprise.RIntellix.ms_risk_engine.domain.entities.simulation;

import java.util.HashMap;

/**
 * Class representing the input features used for scoring. This class
 * encapsulates all the raw data inputs that the scoring model requires to
 * perform its calculations.
 * The features are stored in a HashMap with String keys (feature names) and
 * Object values (feature values), allowing for flexibility in the types of
 * features that can be included.
 * This design allows the model to evolve and include new features without
 * needing to change the class structure, as long as the scoring logic can
 * handle the new features appropriately.
 *
 * @author: Lucía Fernández Mancebo
 *          Date: 03-02-2026
 * @date 27/08/2026
 */
public class ModelInputs {

    private HashMap<String, Object> features;

    /**
     * Default constructor for ModelInputs.
     */
    public ModelInputs() {
        this.features = new HashMap<>();
    }

    /**
     * Parameterized constructor for ModelInputs. Allows setting the features
     * HashMap at initialization.
     *
     * @param features The HashMap containing the input features.
     */
    public ModelInputs(HashMap<String, Object> features) {
        this.features = features;
    }

    // Getters and Setters

    public HashMap<String, Object> getFeatures() {
        return features;
    }

    public void setFeatures(HashMap<String, Object> features) {
        this.features = features;
    }

    // toString, hashCode and equals
    @Override
    public String toString() {
        return "ModelInputs [features=" + features + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((features == null) ? 0 : features.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ModelInputs other = (ModelInputs) obj;
        if (features == null) {
            if (other.features != null)
                return false;
        } else if (!features.equals(other.features))
            return false;
        return true;
    }

}
