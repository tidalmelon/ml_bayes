import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NativeBayes {
    /**
     * 默认频率
     */
    private double defaultFreq = 0.1;
    
    /**
     * 训练数据的比例
     */
    private Double trainingPercent = 0.8;

    private Map<String, List<String>> files_all = new HashMap<String, List<String>>();

    private Map<String, List<String>> files_train = new HashMap<String, List<String>>();

    private Map<String, List<String>> files_test = new HashMap<String, List<String>>();

    public NativeBayes() {

    }

    /**
     * 每个分类的频率
     */
    private Map<String, Integer> classFreq = new HashMap<String, Integer>();
    
    private Map<String, Double> ClassProb = new HashMap<String, Double>();
    
    /**
     * 特征总数
     */
    private Set<String> WordDict = new HashSet<String>();
    
    private Map<String, Map<String, Integer>> classFeaFreq = new HashMap<String, Map<String, Integer>>();
    
    private Map<String, Map<String, Double>> ClassFeaProb = new HashMap<String, Map<String, Double>>();
    
    private Map<String, Double> ClassDefaultProb = new HashMap<String, Double>();
    
    /**
     * 计算准确率
     * @param reallist 真实类别
     * @param pridlist 预测类别
     */
    public void Evaluate(List<String> reallist, List<String> pridlist){
        double correctNum = 0.0;
        for (int i = 0; i < reallist.size(); i++) {
            if(reallist.get(i) == pridlist.get(i)){
                correctNum += 1;
            }
        }
        double accuracy = correctNum / reallist.size();
        System.out.println("准确率为：" + accuracy);
    }
    
    /**
     * 计算精确率和召回率
     * @param reallist
     * @param pridlist
     * @param classname
     */
    public void CalPreRec(List<String> reallist, List<String> pridlist, String classname){
        double correctNum = 0.0;
        double allNum = 0.0;//测试数据中，某个分类的文章总数
        double preNum = 0.0;//测试数据中，预测为该分类的文章总数
        
        for (int i = 0; i < reallist.size(); i++) {
            if(reallist.get(i) == classname){
                allNum += 1;
                if(reallist.get(i) == pridlist.get(i)){
                    correctNum += 1;
                }
            }
            if(pridlist.get(i) == classname){
                preNum += 1;
            }
        }
        System.out.println(classname + " 精确率(跟预测分类比较):" + correctNum / preNum + " 召回率（跟真实分类比较）:" + correctNum / allNum);
    }
    
    /**
     * 用模型进行预测
     */
    public void PredictTestData() {
        List<String> reallist=new ArrayList<String>();
        List<String> pridlist=new ArrayList<String>();
        
        for (Entry<String, List<String>> entry : files_test.entrySet()) {
            String realclassname = entry.getKey();
            List<String> files = entry.getValue();

            
            for (String file : files) {
                reallist.add(realclassname);
                
                
                List<String> classnamelist=new ArrayList<String>();
                List<Double> scorelist=new ArrayList<Double>();
                for (Entry<String, Double> entry_1 : ClassProb.entrySet()) {
                    String classname = entry_1.getKey();
                    //先验概率
                    Double score = Math.log(entry_1.getValue());
                    
                    String[] words = readFromFile(new File(file)).split(" ");
                    for (String word : words) {
                        if(!WordDict.contains(word)){
                            continue;
                        }
                        
                        if(ClassFeaProb.get(classname).containsKey(word)){
                            score += Math.log(ClassFeaProb.get(classname).get(word));
                        }else{
                            score += Math.log(ClassDefaultProb.get(classname));
                        }
                    }
                    
                    classnamelist.add(classname);
                    scorelist.add(score);
                }
                
                Double maxProb = Collections.max(scorelist);
                int idx = scorelist.indexOf(maxProb);
                pridlist.add(classnamelist.get(idx));
            }
        }
        
        Evaluate(reallist, pridlist);
        
        for (String cname : files_test.keySet()) {
            CalPreRec(reallist, pridlist, cname);
        }
        
    }
    
    /**
     * 模型训练
     */
    public void createModel() {
        double sum = 0.0;
        for (Entry<String, Integer> entry : classFreq.entrySet()) {
            sum+=entry.getValue();
        }
        for (Entry<String, Integer> entry : classFreq.entrySet()) {
            ClassProb.put(entry.getKey(), entry.getValue()/sum);
        }
        
        
        for (Entry<String, Map<String, Integer>> entry : classFeaFreq.entrySet()) {
            sum = 0.0;
            String classname = entry.getKey();
            for (Entry<String, Integer> entry_1 : entry.getValue().entrySet()){
                sum += entry_1.getValue();
            }
            double newsum = sum + WordDict.size()*defaultFreq;
            
            Map<String, Double> feaProb = new HashMap<String, Double>();
            ClassFeaProb.put(classname, feaProb);
            
            for (Entry<String, Integer> entry_1 : entry.getValue().entrySet()){
                String word = entry_1.getKey();
                feaProb.put(word, (entry_1.getValue() +defaultFreq) /newsum);
            }
            ClassDefaultProb.put(classname, defaultFreq/newsum);
        }
    }
    
    /**
     * 加载训练数据
     */
    public void loadTrainData(){
        for (Entry<String, List<String>> entry : files_train.entrySet()) {
            String classname = entry.getKey();
            List<String> docs = entry.getValue();
            
            classFreq.put(classname, docs.size());
            
            Map<String, Integer> feaFreq = new HashMap<String, Integer>();
            classFeaFreq.put(classname, feaFreq);
            
            for (String doc : docs) {
                String[] words = readFromFile(new File(doc)).split(" ");
                for (String word : words) {
                    
                    WordDict.add(word);
                    
                    if(feaFreq.containsKey(word)){
                        int num = feaFreq.get(word) + 1;
                        feaFreq.put(word, num);
                    }else{
                        feaFreq.put(word, 1);
                    }
                }
            }    
            
            
        }
        System.out.println(classFreq.size()+" 分类, " + WordDict.size()+" 特征词");
    }
    
    /**
     * 将数据分为训练数据和测试数据
     * 
     * @param dataDir
     */
    public void splitData(String dataDir) {
        // 用文件名区分类别
        Pattern pat = Pattern.compile("\\d+([a-z]+?)\\.");
        dataDir = "testdata/allfiles";
        File f = new File(dataDir);
        File[] files = f.listFiles();
        for (File file : files) {
            String fname = file.getName();
            Matcher m = pat.matcher(fname);
            if (m.find()) {
                String cname = m.group(1);
                if (files_all.containsKey(cname)) {
                    files_all.get(cname).add(file.toString());
                } else {
                    List<String> tmp = new ArrayList<String>();
                    tmp.add(file.toString());
                    files_all.put(cname, tmp);
                }
            } else {
                System.out.println("err: " + file);
            }
        }

        System.out.println("统计数据:");
        for (Entry<String, List<String>> entry : files_all.entrySet()) {
            String cname = entry.getKey();
            List<String> value = entry.getValue();
            // System.out.println(cname + " : " + value.size());

            List<String> train = new ArrayList<String>();
            List<String> test = new ArrayList<String>();

            for (String str : value) {
                if (Math.random() <= trainingPercent) {// 80%用来训练 , 20%测试
                    train.add(str);
                } else {
                    test.add(str);
                }
            }

            files_train.put(cname, train);
            files_test.put(cname, test);
        }

        System.out.println("所有文件数:");
        printStatistics(files_all);
        System.out.println("训练文件数:");
        printStatistics(files_train);
        System.out.println("测试文件数:");
        printStatistics(files_test);

    }

    /**
     * 打印统计信息
     * 
     * @param m
     */
    public void printStatistics(Map<String, List<String>> m) {
        for (Entry<String, List<String>> entry : m.entrySet()) {
            String cname = entry.getKey();
            List<String> value = entry.getValue();
            System.out.println(cname + " : " + value.size());
        }
        System.out.println("--------------------------------");
    }

    public static String readFromFile(File file) {
        Long filelength = file.length();
        byte[] content = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(content);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String c = new String(content, "utf-8");
            return c.trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        NativeBayes bayes = new NativeBayes();
        bayes.splitData(null);
        bayes.loadTrainData();
        bayes.createModel();
        bayes.PredictTestData();

    }

}
