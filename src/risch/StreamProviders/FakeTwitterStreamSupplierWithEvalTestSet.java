package risch.StreamProviders;

import org.json.JSONObject;
import risch.StreamSupplier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by johan.risch on 09/06/15.
 */
public class FakeTwitterStreamSupplierWithEvalTestSet implements StreamSupplier {
    private final String mPath;
    private final String mSpamPath;
    private final float mAmountOfSpam;
    Queue<JSONObject> msgs = new ArrayBlockingQueue<JSONObject>(400);
    private Scanner mScanner;
    private Scanner mSpamScanner;

    public FakeTwitterStreamSupplierWithEvalTestSet(String filepath) {
        mPath = filepath;
        mSpamPath = null;
        mAmountOfSpam = 0;
    }

    public FakeTwitterStreamSupplierWithEvalTestSet(String filepath, String spamFile, float amountOfSpam) {
        mPath = filepath;
        mSpamPath = spamFile;
        mAmountOfSpam = amountOfSpam;

    }

    @Override
    public void setup() {
        try {
            mScanner = new Scanner(new File(mPath));
            if(mSpamPath != null){
                mSpamScanner = new Scanner(new File(mSpamPath));
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNext(int minFollowersCount) throws InterruptedException {
        if(mSpamPath != null && Math.random() < mAmountOfSpam && mSpamScanner.hasNextLine()){
            return "spam;;;"+mSpamScanner.nextLine();
        }
        if (mScanner.hasNextLine()) {
            return mScanner.nextLine();

        }
        return null;
    }
}
