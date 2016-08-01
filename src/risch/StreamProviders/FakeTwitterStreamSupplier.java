package risch.StreamProviders;

import org.json.JSONArray;
import org.json.JSONObject;
import risch.StreamSupplier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by johan.risch on 09/06/15.
 */
public class FakeTwitterStreamSupplier implements StreamSupplier {
    private final String mPath;
    Queue<JSONObject> msgs = new ArrayBlockingQueue<JSONObject>(400);
    private Scanner mScanner;
    public FakeTwitterStreamSupplier(String filepath){
        mPath = filepath;
    }

    @Override
    public void setup() {
        try {
            mScanner = new Scanner(new File(mPath));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNext(int minFollowersCount) throws InterruptedException {
        if(mScanner.hasNextLine()){
            return mScanner.nextLine();
        }
        return null;
    }
}
