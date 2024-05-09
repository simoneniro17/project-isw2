package utils;

//  Utility class for managing project properties
public class Properties {
    public static final String COMMON_PROJECT = "BOOKKEEPER";
    public static final String CUSTOM_PROJECT = "STORM";
    public static final String DATASET_FILE = "dataset.csv";
    public static final String FILE_EXTENSION = ".java";
    public static final String PROJECT_PATH = System.getProperty("user.dir");
    public static final String OUTPUT_DIRECTORY = PROJECT_PATH + "/src/main/java/outputs/";
    
    public static final String GIT_HUB_URL = "https://github.com/apache/";
    
    private Properties() {
    }
}