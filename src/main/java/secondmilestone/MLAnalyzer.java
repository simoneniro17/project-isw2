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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static secondmilestone.AcumeUtils.getNpofb;

public class MLAnalyzer {
    private final String projectName;
    private final String datasetArff;
    private static String npofbValue = null;
    
    public MLAnalyzer(String projectName) {
        this.projectName = projectName;
        datasetArff = Properties.OUTPUT_DIRECTORY + projectName + Properties.DATASET_FILE_ARFF;
    }
    
    public void performAnalysis() throws Exception {
        DataSource source = new DataSource(datasetArff);
        Instances dataset = source.getDataSet();
        dataset.deleteStringAttributes();
        
        DataFilter dataFilter = new DataFilter();
        int numberOfAttributes = dataset.numAttributes();
        int numberOfVersions = dataset.attribute(0).numValues();
        List<MLModelEval> modelEvaluations = new ArrayList<>();
        Evaluation evaluation;
        
        // feature selection methods iteration
        for (MLProfile.FEATURE_SELECTION featureSelection : MLProfile.FEATURE_SELECTION.values()) {
            
            // sampling methods iteration
            for (MLProfile.BALANCING balancing : MLProfile.BALANCING.values()) {
                
                // cost sensitive methods iteration
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
                            modelEvaluations.add(new MLModelEval(classifier, featureSelection, balancing, sensitivity, evaluation, npofbValue));
                        }
                    }
                }
            }
        }
        
        Printer.printMessage("Evaluation completed!\n");
        Printer.printMessage("Writing results to CSV...");
        writeResultsToCSV(modelEvaluations, numberOfVersions);
        Printer.printMessage("Results written!\n");
    }
    
    /**
     * Executes the analysis using the specified classifier and sensitivity.
     *
     * @param trainingSet the training set
     * @param testingSet  the testing set
     * @param classifier  the classifier to use
     * @param sensitivity the sensitivity setting
     * @return the evaluation results
     * @throws Exception if an error occurs during the analysis
     */
    private static Evaluation executeAnalysis(Instances trainingSet, Instances testingSet, MLProfile.CLASSIFIER classifier, MLProfile.SENSITIVITY sensitivity) throws Exception {
        Classifier cls;
        
        if (classifier.equals(MLProfile.CLASSIFIER.RANDOM_FOREST))
            cls = new RandomForest();
        else if (classifier.equals(MLProfile.CLASSIFIER.NAIVE_BAYES))
            cls = new NaiveBayes();
        else
            cls = new IBk();
        
        Evaluation evaluation;
        
        if (sensitivity.equals(MLProfile.SENSITIVITY.NO_COST_SENSITIVE)) {
            // classifier training
            cls.buildClassifier(trainingSet);
            
            // model evaluation
            evaluation = new Evaluation(testingSet);
            evaluation.evaluateModel(cls, testingSet);
            
            // calculate NPofB20
            npofbValue = AcumeUtils.getNpofb(testingSet, cls);
        } else {
            CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
            CostMatrix costMatrix = createCostMatrix(1.0, 10.0);
            costSensitiveClassifier.setClassifier(cls);
            costSensitiveClassifier.setCostMatrix(costMatrix);
            
            // in case of "sensitive learning", the equals() method will return false
            costSensitiveClassifier.setMinimizeExpectedCost(sensitivity.equals(MLProfile.SENSITIVITY.SENSITIVE_THRESHOLD));
            costSensitiveClassifier.buildClassifier(trainingSet);
            
            evaluation = new Evaluation(testingSet, costSensitiveClassifier.getCostMatrix());
            evaluation.evaluateModel(costSensitiveClassifier, testingSet);
            
            // calculate NPofB20
            npofbValue = AcumeUtils.getNpofb(testingSet, cls);
        }
        
        npofbValue = getNpofb(testingSet, cls);
        
        return evaluation;
    }
    
    /**
     * Creates a cost matrix.
     *
     * @param weightFalsePositive the weight for false positives
     * @param weightFalseNegative the weight for false negatives
     * @return the cost matrix
     */
    private static CostMatrix createCostMatrix(double weightFalsePositive, double weightFalseNegative) {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, weightFalsePositive);
        costMatrix.setCell(0, 1, weightFalseNegative);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }
    
    /**
     * Writes the evaluation results to a CSV file.
     *
     * @param modelEvaluations the list of model evaluations
     * @param numVersions      the number of versions in the dataset
     */
    private void writeResultsToCSV(List<MLModelEval> modelEvaluations, int numVersions) {
        String outFileName = Properties.OUTPUT_DIRECTORY + projectName + "results.csv";
        
        try (FileWriter fileWriter = new FileWriter(outFileName)) {
            fileWriter.append("Dataset,#TrainingRelease,Classifier,Feature Selection,Balancing,Sensitivity,Accuracy,Precision,Recall,AUC,Kappa,NPofB20\n");
            int numberOfTrainingRelease = 1;
            int counter = 0;
            
            for (MLModelEval eval : modelEvaluations) {
                // reset training release after iterating over classifiers (3 is the number of classifiers)
                if (counter >= 3) {
                    if (numberOfTrainingRelease >= numVersions - 1)
                        numberOfTrainingRelease = 1;
                    else
                        numberOfTrainingRelease++;
                    
                    counter = 0;
                }
                
                String classifier = eval.getClassifier().toString();
                String featureSelection = eval.getFeatureSelection().toString();
                String balancing = eval.getBalancing().toString();
                String sensitivity = eval.getSensitivity().toString();
                
                Evaluation evaluation = eval.getEvaluation();
                String accuracy = String.format(Locale.US, "%.3f", evaluation.pctCorrect());
                String precision = String.format(Locale.US, "%.3f", evaluation.precision(1));
                String recall = String.format(Locale.US, "%.3f", evaluation.recall(1));
                String auc = String.format(Locale.US, "%.3f", evaluation.areaUnderROC(1));
                String kappa = String.format(Locale.US, "%.3f", evaluation.kappa());
                String npofb20 = eval.getNpofb20().length() > 5 ? eval.getNpofb20().substring(0, 5) : eval.getNpofb20();
                
                String line = String.format("%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n", projectName, numberOfTrainingRelease,
                        classifier, featureSelection, balancing, sensitivity, accuracy, precision, recall, auc, kappa, npofb20);
                
                if (!precision.equals("NaN") && !auc.equals("NaN"))
                    fileWriter.append(line);
                counter++;
            }
        } catch (IOException e) {
            Printer.printError("Error in writing results in CSV");
        }
    }
}