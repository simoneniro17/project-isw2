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
    
    public void applyFeatureSelection(MLProfile.FEATURE_SELECTION featureSelection) throws Exception {
        if (featureSelection.equals(MLProfile.FEATURE_SELECTION.BEST_FIRST)) {
            AttributeSelection attributeSelection = new AttributeSelection();
            attributeSelection.setEvaluator(new CfsSubsetEval());
            attributeSelection.setSearch(new BestFirst());
            attributeSelection.SelectAttributes(trainingData);
            
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndicesArray(attributeSelection.selectedAttributes());
            removeFilter.setInvertSelection(true);
            removeFilter.setInputFormat(trainingData);
            
            trainingData = Filter.useFilter(trainingData, removeFilter);
            testingData = Filter.useFilter(testingData, removeFilter);
        }
    }
    
    public void applySampling(MLProfile.BALANCING balancing) throws Exception {
        if (balancing.equals(MLProfile.BALANCING.OVERSAMPLING)) {
            Resample resample = new Resample();
            resample.setInputFormat(trainingData);
            
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            resample.setOptions(Utils.splitOptions(String.format("%s %s", "-B 1.0 -Z", decimalFormat.format(calculateMajorityClassPercentage()))));
            trainingData = Filter.useFilter(trainingData, resample);
        } else if (balancing.equals(MLProfile.BALANCING.UNDERSAMPLING)) {
            SpreadSubsample underSampling = new SpreadSubsample();
            
            underSampling.setInputFormat(trainingData);
            underSampling.setOptions(Utils.splitOptions("-M 1.0"));
            trainingData = Filter.useFilter(trainingData, underSampling);
        } else if (balancing.equals(MLProfile.BALANCING.SMOTE)) {
            SMOTE smote = new SMOTE();
            smote.setInputFormat(trainingData);
            trainingData = Filter.useFilter(trainingData, smote);
        }
    }
    
    /* PER OVERSAMPLING
    noReplacement=false, biasToUniformClass=1.0, and sampleSizePercent=Y, Y = 100 * (majority –minority)/minority.
    Example for the diabetes data: weka.filters.supervised.instance.Resample-B 1.0 -Z 130.3
     */
    private double calculateMajorityClassPercentage() {
        int numberOfBuggy = 0;
        String buggy;
        
        Instances dataset = new Instances(trainingData);
        dataset.addAll(trainingData);
        
        for(Instance instance : dataset) {
            buggy = instance.stringValue(dataset.numAttributes() - 1);
            if(buggy.equals("1"))
                numberOfBuggy++;
        }
        
        double percentage = (100 * 2 * numberOfBuggy) / dataset.size();
        if (percentage >= 50)
            return percentage;
        else
            return 100 - percentage;
    }
    
    public Instances getTrainingSet(Instances dataset, int trainingRelease, int releases) throws Exception {
        RemoveWithValues removeWithValues = new RemoveWithValues();
        int range = releases - trainingRelease;
        int[] arr = new int[range];
        
        for (int i = 1; i < range + 1; i++) {
            arr[range - i] = releases - i;
        }
        
        removeWithValues.setAttributeIndex("1");
        removeWithValues.setNominalIndicesArr(arr);
        removeWithValues.setInputFormat(dataset);
        
        return Filter.useFilter(dataset, removeWithValues);
    }
    
    public Instances getTestingSet(Instances dataset, int trainingRelease) throws Exception {
        String options = String.format("-C 1 -L %d -V", trainingRelease + 1);
        RemoveWithValues removeWithValues = new RemoveWithValues();
        
        removeWithValues.setOptions(Utils.splitOptions(options));
        removeWithValues.setInputFormat(dataset);
        
        return Filter.useFilter(dataset, removeWithValues);
    }
}
