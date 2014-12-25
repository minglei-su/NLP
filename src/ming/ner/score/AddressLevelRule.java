package ming.ner.score;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mingleis on 2014/12/25.
 */
public class AddressLevelRule {


    private Map<String, String[]> addressLevelDict;
    private String addressLevelDictPath = "./score/addressLevelDict.txt";
    private static String SPILIT = ",";
    private static String SPILITKEY = ":";
    private static float MATCHSCORE = 0.6f;

    public AddressLevelRule() {
        super();
        this.loadAddressLevelDict();
    }
    /**
     * 地址等级  A，B,C  A为省份，B,C为地市  文件结构为A:B,C 加载进内存结构为A:BC B:A C:A
     * 地址分析，根据等级
     *
     * @return
     */
    private boolean loadAddressLevelDict() {
        try {
            addressLevelDict = new HashMap<String, String[]>();
            File file = new File(addressLevelDictPath);
            FileReader reader = new FileReader(file);
            BufferedReader bufferedInputStream = new BufferedReader(reader);
            String cur = bufferedInputStream.readLine();
            while (cur != null && cur != "") {
                String[] curarray = cur.split(SPILITKEY);
                if (curarray.length > 1) {//当前行个数大于2
                    String bigLevel = curarray[0];
                    String[] smallLevels = curarray[1].split(SPILIT);
                    if (addressLevelDict.get(bigLevel) != null) {//该key已存在
                        String[] smallLevelArray = addressLevelDict.get(bigLevel);
                        String[] newSmallLevelArray = new String[smallLevelArray.length + smallLevels.length];
                        System.arraycopy(smallLevelArray, 0, newSmallLevelArray, 0, smallLevelArray.length);
                        System.arraycopy(smallLevels, 0, newSmallLevelArray, smallLevelArray.length, smallLevels.length);
                        addressLevelDict.put(bigLevel, newSmallLevelArray);
                    } else {
                        addressLevelDict.put(bigLevel, smallLevels);
                    }
                    //B:A,C:A
                    for (String smallLevel : smallLevels) {
                        if (addressLevelDict.get(smallLevel) != null) {//该key已存在
                            String[] sbigLevelArray = addressLevelDict.get(smallLevel);
                            String[] newBigLevelArray = new String[sbigLevelArray.length + 1];
                            System.arraycopy(sbigLevelArray, 0, newBigLevelArray, 0, sbigLevelArray.length);
                            newBigLevelArray[sbigLevelArray.length] = bigLevel;
                            addressLevelDict.put(smallLevel, newBigLevelArray);
                        } else {
                            addressLevelDict.put(smallLevel, new String[]{bigLevel});
                        }
                    }
                }
                cur = bufferedInputStream.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public float compareWords(String word1, String word2) {
        float score = 0.0f;
        for (String tempeach : addressLevelDict.get(word1)) {
            if (word2.equalsIgnoreCase(tempeach)) {
                score = MATCHSCORE;
                break;
            }
        }
        return score;
    }
}