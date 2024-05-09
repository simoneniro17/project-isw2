package model;

// Profile for ML evaluation defined by Specifiche Falessi at 09/05/2024
public class Profile {
    private Profile() {}
    
    public enum CLASSIFIER {
        RANDOM_FOREST,
        NAIVE_BAYES,
        IBK
    }
    // Comparare l'accuratezza su Precision/Recall/AUC/Kappa/NPofB20 dei tre classificatori utilizzando Walk Forward
    // si consiglia di creare, e poi analizzare, un file avente le seguenti colonne:
    // dataset, #TrainingRelease, Classifier, Precision, Recall, AUC, Kappa, NPofB20
    
    public enum FEATURE_SELECTION {
        NO_SELECTION,
        BEST_FIRST
    }
    
    public enum BALANCING {
        NO_SAMPLING,
        OVERSAMPLING,
        UNDERSAMPLING,
        SMOTE
    }
    
    public enum SENSITIVITY {
        NO_COST_SENSITIVE,
        SENSITIVE_THRESHOLD,
        SENSITIVE_LEARNING
    }
}
