package secondmilestone;

import model.MLProfile;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

import java.text.DecimalFormat;

public class DataFilter {
    Instances trainingData;
    Instances testingData;
    
    /**
     * Applies feature selection to the training and testing datasets.
     *
     * @param featureSelection the feature selection method to apply
     * @throws Exception if an error occurs during feature selection
     */
    public void applyFeatureSelection(MLProfile.FEATURE_SELECTION featureSelection) throws Exception {
        if (featureSelection.equals(MLProfile.FEATURE_SELECTION.BEST_FIRST)) {
            // "best first" attribute selection method using CfsSubsetEval as the evaluator
            AttributeSelection attributeSelection = new AttributeSelection();
            attributeSelection.setEvaluator(new CfsSubsetEval());
            attributeSelection.setSearch(new BestFirst());
            
            // attributes selection based on the training dataset
            attributeSelection.SelectAttributes(trainingData);
            
            // removing non-selected attributes from both training and testing datasets
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndicesArray(attributeSelection.selectedAttributes());
            removeFilter.setInvertSelection(true);
            removeFilter.setInputFormat(trainingData);
            
            trainingData = Filter.useFilter(trainingData, removeFilter);
            testingData = Filter.useFilter(testingData, removeFilter);
        }
        // else it would be "no selection", i.e. nothing to do
    }
    
    /**
     * Applies sampling techniques to balance the training dataset.
     *
     * @param balancing the balancing method to apply
     * @throws Exception if an error occurs during sampling
     */
    public void applySampling(MLProfile.BALANCING balancing) throws Exception {
        if (balancing.equals(MLProfile.BALANCING.OVERSAMPLING)) {
            // resample filter for oversampling
            Resample resample = new Resample();
            resample.setInputFormat(trainingData);
            
            /*  Options for Resample filter to oversample:
                Bias (B) rate of 1.0 (i.e. balanced)
                (Z) oversampling with substitution (instances repetition)
                Percentage value calculated based on the majority class percentage compared to the minority class
             */
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String options = String.format("%s %s", "-B 1.0 -Z", decimalFormat.format(calculateMajorityClassPercentage()));
            resample.setOptions(Utils.splitOptions(options));
            
            // Resample filter application to training set
            trainingData = Filter.useFilter(trainingData, resample);
        } else if (balancing.equals(MLProfile.BALANCING.UNDERSAMPLING)) {
            // SpreadSubsample filter for undersampling
            SpreadSubsample underSampling = new SpreadSubsample();
            underSampling.setInputFormat(trainingData);
            underSampling.setOptions(Utils.splitOptions("-M 1.0"));
            
            // SpreadSubsample filter application to training set
            trainingData = Filter.useFilter(trainingData, underSampling);
        } else if (balancing.equals(MLProfile.BALANCING.SMOTE)) {
            // SMOTE filter for synthetic minority oversampling technique
            SMOTE smote = new SMOTE();
            smote.setInputFormat(trainingData);
            
            // SMOTE filter application to training set
            trainingData = Filter.useFilter(trainingData, smote);
        }
    }
    
    /**
     * Calculates the percentage of the majority class in the dataset for oversampling.
     *
     * @return the percentage of the majority class
     */
    private double calculateMajorityClassPercentage() {
        int numberOfBuggy = 0;
        String buggy;
        
        Instances dataset = new Instances(trainingData);
        dataset.addAll(trainingData);
        
        // loop through instances to count the number of instances belonging to the majority class
        for (Instance instance : dataset) {
            buggy = instance.stringValue(dataset.numAttributes() - 1);
            if (buggy.equals("1"))
                numberOfBuggy++;
        }
        
        // percentage of the majority class
        int percentage = (100 * 2 * numberOfBuggy) / dataset.size();
        if (percentage >= 50)
            return percentage;
        else
            return 100.0 - percentage;
    }
    
    /**
     * Gets the training set from the dataset based on the training release and total releases.
     *
     * @param dataset         the dataset to extract the training set from
     * @param trainingRelease the index of the training release
     * @param releases        the total number of releases
     * @return the training set
     * @throws Exception if an error occurs while getting the training set
     */
    public Instances getTrainingSet(Instances dataset, int trainingRelease, int releases) throws Exception {
        // remove instances from the dataset corresponding to the testing releases
        RemoveWithValues removeWithValues = new RemoveWithValues();
        int range = releases - trainingRelease;
        int[] arr = new int[range];
        
        // array of indices corresponding to the testing releases
        for (int i = 1; i < range + 1; i++)
            arr[range - i] = releases - i;
        
        // options to remove instances corresponding to testing releases
        removeWithValues.setAttributeIndex("1");
        removeWithValues.setNominalIndicesArr(arr);
        removeWithValues.setInputFormat(dataset);
        
        // apply RemoveWithValues filter to get the training set
        return Filter.useFilter(dataset, removeWithValues);
    }
    
    
    /**
     * Gets the testing set from the dataset based on the training release.
     *
     * @param dataset         the dataset to extract the testing set from
     * @param trainingRelease the index of the training release
     * @return the testing set
     * @throws Exception if an error occurs while getting the testing set
     */
    public Instances getTestingSet(Instances dataset, int trainingRelease) throws Exception {
        /* options to remove instances corresponding to the training release:
           (-C 1) attribute index,
           (-L %d) upper limit for nominal values
           (-V) retain missing values
         */
        String options = String.format("-C 1 -L %d -V", trainingRelease + 1);
        RemoveWithValues removeWithValues = new RemoveWithValues();
        
        removeWithValues.setOptions(Utils.splitOptions(options));
        removeWithValues.setInputFormat(dataset);
        
        // apply RemoveWithValues filter to get the testing set
        return Filter.useFilter(dataset, removeWithValues);
    }
}
