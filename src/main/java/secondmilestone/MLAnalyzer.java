package secondmilestone;

import model.MLModelEval;
import model.MLProfile;
import utils.Printer;
import utils.Properties;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;

public class MLAnalyzer {
    private final String datasetArff;
    
    public MLAnalyzer(String projectName) {
        datasetArff = Properties.OUTPUT_DIRECTORY + projectName + Properties.DATASET_FILE_ARFF;
    }
    
    public void loadDataset() throws Exception {
        DataSource source = new DataSource(datasetArff);
        Instances dataset = source.getDataSet();
        dataset.deleteStringAttributes();
        
        DataFilter dataFilter = new DataFilter();
        int numberOfAttributes = dataset.numAttributes();
        int numberOfVersions = dataset.attribute(0).numValues();
        List<MLModelEval> modelEvaluations = new ArrayList<>();
        Evaluation evaluation;
        
        // feature selection iteration
        for (MLProfile.FEATURE_SELECTION featureSelection : MLProfile.FEATURE_SELECTION.values()) {
            
            // sampling iteration
            for (MLProfile.BALANCING balancing : MLProfile.BALANCING.values()) {
                
                // cost sensitive iteration
                for (MLProfile.SENSITIVITY sensitivity : MLProfile.SENSITIVITY.values()) {
                    
                    // walk forward
                    for (int i = 1; i < numberOfVersions; i++) {
                        Instances training = dataFilter.getTrainingSet(dataset, i, numberOfVersions);
                        Instances testing = dataFilter.getTestingSet(dataset, i);
                        
                        training.deleteAttributeAt(0);
                        testing.deleteAttributeAt(0);
                        training.setClassIndex(numberOfAttributes - 2);
                        testing.setClassIndex(numberOfAttributes - 2);
                        
                        dataFilter.trainingData = training;
                        dataFilter.testingData = testing;
                        
                        // apply feature selection and sampling
                        dataFilter.applyFeatureSelection(featureSelection);
                        dataFilter.applySampling(balancing);
                        
                        training = dataFilter.trainingData;
                        testing = dataFilter.testingData;
                        
                        // using classifiers
                        for (MLProfile.CLASSIFIER classifier : MLProfile.CLASSIFIER.values()) {
                            evaluation = executeAnalysis(training, testing, classifier, sensitivity);
                            
                            Printer.printCLI("(" + i + ") Classificatore: " + classifier
                                    + "\t\tFeature Selection: " + featureSelection
                                    + "\t\tBalancing: " + balancing
                                    + "\t\tSensitivity: " + sensitivity);
                            Printer.printCLI("\nAccuratezza: " + evaluation.pctCorrect() + "%");
                            Printer.printCLI("\nPrecision: " + evaluation.precision(1));
                            Printer.printCLI("\nRecall: " + evaluation.recall(1));
                            Printer.printCLI("\nAUC: " + evaluation.areaUnderROC(1));
                            Printer.printCLI("\nKappa: " + evaluation.kappa() + "\n\n");
                            
                            modelEvaluations.add(new MLModelEval(classifier, featureSelection, balancing, sensitivity, evaluation));
                        }
                    }
                }
            }
        }
    }
    
    private static Evaluation executeAnalysis(Instances trainingSet, Instances testingSet, MLProfile.CLASSIFIER classifier, MLProfile.SENSITIVITY sensitivity) throws Exception {
        Classifier cls;
        
        if (classifier.equals(MLProfile.CLASSIFIER.RANDOM_FOREST))
            cls = new RandomForest();
        else if (classifier.equals(MLProfile.CLASSIFIER.NAIVE_BAYES))
            cls = new NaiveBayes();
        else
            cls = new IBk();
        
        Evaluation evaluation;
        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        CostMatrix costMatrix = createCostMatrix(1.0, 10.0);
        costSensitiveClassifier.setClassifier(cls);
        costSensitiveClassifier.setCostMatrix(costMatrix);
        
        if (sensitivity.equals(MLProfile.SENSITIVITY.NO_COST_SENSITIVE)) {
            // classifier training
            cls.buildClassifier(trainingSet);
            
            // model evaluation
            evaluation = new Evaluation(testingSet);
            evaluation.evaluateModel(cls, testingSet);
        } else {
            //TODO FARLO ANCHE CON ALTRA SENSITIVITY
            costSensitiveClassifier.setMinimizeExpectedCost(sensitivity.equals(MLProfile.SENSITIVITY.SENSITIVE_THRESHOLD));
            costSensitiveClassifier.buildClassifier(trainingSet);
            
            evaluation = new Evaluation(testingSet, costSensitiveClassifier.getCostMatrix());
            evaluation.evaluateModel(costSensitiveClassifier, testingSet);
        }
        
        return evaluation;
    }
    
    private static CostMatrix createCostMatrix(double weightFalsePositive, double weightFalseNegative) {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, weightFalsePositive);
        costMatrix.setCell(0, 1, weightFalseNegative);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }
    
    /*
    CostSensitiveClassifierc1 = new CostSensitiveClassifier();
    c1.setClassifier(new J48());
    c1.setCostMatrix( createCostMatrix(CFP, CFN));
    c1.buildClassifier(data);
    Evaluation ec1 = new Evaluation(data,c1.getCostMatrix());
    ec1.evaluateModel(c1, data);
     */
    
    //private static void writeResultsToCSV

}
