package risch;

import org.tartarus.snowball.ext.englishStemmer;
import risch.StreamProviders.FakeTwitterStreamSupplierWithEval;
import risch.StreamProviders.FakeTwitterStreamSupplierWithEvalTestSet;
import risch.online.OnlineLDA;
import risch.online.Result;
import risch.online.RunningStat;
import risch.tokens.Documents;
import risch.tokens.PlainVocabulary;
import risch.tokens.Tuple;
import risch.tokens.Vocabulary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    public static HashMap<String, Object> argMap = new HashMap<>();
    public static HashMap<String, String> topicsMap = new HashMap<>();
    private static String consumerKey = "J5ZcsmHaRjOio57InGShpRePS";
    private static String consumerSecret = "A8cS00FlBB1b9Fv0G1ekDd3daIizcWigJo55Hcp3FQ1epiMf95";
    private static String token = "718801825-4iqcfpl8rWRvzkaCKC5mR32xuRrImd45ClteyT5B";
    private static String secret = "7BIfCh90Dytkritw18Kfu6nB0dKuoZEqYDcxyHAnHkeXY";
    private static int D;
    private static int K;
    private static int batchSize;
    private static double tau;
    private static double kappa;
    private static double alpha;
    private static double eta;
    private static String dictPath;
    private static Worker worker;
    private static String dataFile;
    private static float spamLevel = 0.7f;
    private static ArrayList<String>[] data;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 0) {
            System.out.println("please provide paths for document directory and dictionary file");
            return;
        }
        for (String s : args) {
            String[] keyVal = s.split(":");
            if (keyVal.length > 1) {
                argMap.put(keyVal[0], keyVal[1]);
            }

        }
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);


        dictPath = argMap.containsKey("dict") ? argMap.get("dict").toString() : "/Users/johan.risch/Documents/Exjobb/jsStream/words/combined.txt";//"/Users/johan.risch/Documents/Exjobb/jsStream/words/combined.txt";//args[0];


        D = argMap.containsKey("D") ? Integer.parseInt(argMap.get("D").toString()) : 170000;
        K = argMap.containsKey("K") ? Integer.parseInt(argMap.get("K").toString()) : 4;
        batchSize = argMap.containsKey("batchSize") ? Integer.parseInt(argMap.get("batchSize").toString()) : 100;


        tau = argMap.containsKey("tau") ? Double.parseDouble(argMap.get("tau").toString()) : 0.9d;


        kappa = argMap.containsKey("kappa") ? Double.parseDouble(argMap.get("kappa").toString()) : 0.9d;

        alpha = argMap.containsKey("alpha") ? Double.parseDouble(argMap.get("alpha").toString()) : 0.15d;

        eta = argMap.containsKey("eta") ? Double.parseDouble(argMap.get("eta").toString()) : 0.15d;

        dataFile = argMap.containsKey("data") ? argMap.get("data").toString() : "/Users/johan.risch/Documents/Exjobb/jsStream/topicDumps/combined.txt";
        String spamfile = "/Users/johan.risch/Documents/Exjobb/jsStream/streamDumps/2015-06-16_1100.txt";
        worker = new Worker(new FakeTwitterStreamSupplierWithEval(dataFile, spamfile, spamLevel));
        worker.setShouldCat(false);
        if (argMap.containsKey("blocking")) {
            worker.topicCount = new int[K + 1];

            System.out.println("Current values");
            System.out.println("D = " + D);
            System.out.println("K = " + K);
            System.out.println("batchSize = " + batchSize);
            System.out.println("tau = " + tau);
            System.out.println("kappa = " + kappa);
            System.out.println("alpha = " + alpha);
            System.out.println("eta = " + eta);
            System.out.println("cat = " + worker.shouldCat());
            System.out.println("spamLevel = " + spamLevel);

            worker.run();

            String res = "" + worker.getResult();

            Set<String> strings = topicsMap.keySet();
            for (String k : strings) {
                res = res.replace(k, topicsMap.get(k));
            }

            System.out.println(res);
            System.out.println(worker.getTweetsSeen() + " tweets seen");
            System.out.println(worker.temp + " temp");

            return;
        }


        System.out.println("Use default values? (y/n)");
        String def = br.readLine();


        if (def.equalsIgnoreCase("n")) {
            String val = "";
            System.out.println("Set values by typing '{variable}={value}' when finished, type 'done'");
            while (true) {
                val = br.readLine();
                setVal(val);
                if (val.equalsIgnoreCase("done")) {
                    break;
                }

            }


        }
        if (def.equalsIgnoreCase("y")) {
            System.out.println("Final values:");
            System.out.println("D = " + D);
            System.out.println("K = " + K);
            System.out.println("batchSize = " + batchSize);
            System.out.println("tau = " + tau);
            System.out.println("kappa = " + kappa);
            System.out.println("alpha = " + alpha);
            System.out.println("eta = " + eta);
            System.out.println("spamLevel = " + spamLevel);
        }
        data = new ArrayList[K];
        worker.topicCount = new int[K + 1];
        new Thread(worker).start();


        while (true) {
            String command = br.readLine();
            if (command.equalsIgnoreCase("h")) {
                System.out.println("p: print current result\n" +
                        "pNames: print names of topics\n" +
                        "set {variable}={value}\n" +
                        "cat {textToCategorize}: attempt to categorize a text\n" +
                        "name {topicNumber}={topicName}: names topic with number topicNumber to topicName\n" +
                        "show {topicNumber}: shows the 20 last tweets cateorized into topic with index topicNumber\n" +
                        "showTd: show topic distribution\n" +
                        "quit: finish execution");

            }

            if (command.equalsIgnoreCase("p")) {
                String res = "" + worker.getResult();

                Set<String> strings = topicsMap.keySet();
                for (String k : strings) {
                    res = res.replace(k, topicsMap.get(k));
                }

                System.out.println(res);
                System.out.println(worker.getTweetsSeen() + " tweets seen");
                System.out.println(worker.temp + " temp");
                System.out.println("lastLoopTime: " + sDelta + " ms, " + (double) batchSize / ((double) sDelta / 1000d) + " docs/s");
            } else if (command.equalsIgnoreCase("pNames")) {
                Set<String> strings = topicsMap.keySet();
                for (String k : strings) {
                    System.out.println(k + " : " + topicsMap.get(k));
                }
            } else if (command.startsWith("set")) {
                setVal(command.substring(4));
            } else if (command.startsWith("name")) {
                String name = command.substring(5);
                String[] splitted = name.split("=");
                int topic = Integer.parseInt(splitted[0]);
                String newName = splitted[1];
                System.out.println("Naming Topic " + topic + " to " + newName);
                topicsMap.put("Topic " + topic, newName);

            } else if (command.startsWith("cat")) {
                String text = command.substring(4);
                text = text.toLowerCase();
                text = text.replaceAll("[^a-zA-Z0-9 @]", "");
                categorize(worker.getResult(), text, true);
            } else if (command.equalsIgnoreCase("quit")) {
                worker.serRunning(false);
                break;
            } else if (command.startsWith("show ")) {
                int topic = Integer.parseInt(command.substring(5));
                HashMap<Integer, LinkedList<String>> cats = worker.getCategorized();
                if (cats.containsKey(topic)) {
                    for (String s : cats.get(topic)) {
                        System.out.println(s);
                    }
                }
            } else if (command.equalsIgnoreCase("showTd")) {
                int[] cats = worker.getTopicCount();
                double totalProb = 0;
                for (int i = 0; i < cats.length; i++) {
                    double prob = (double) cats[i] / (double) worker.getTweetsSeen();
                    prob *= 100;
                    System.out.println("Topic " + i + ": " + prob + "%");
                    totalProb += prob;
                }
                System.out.println("Total prob: " + totalProb + "%");

            } else if (command.startsWith("set")) {
                String[] split = command.split(" ");
                setVal(split[1] + "=" + split[2]);
            } else if (command.startsWith("eval")) {//eval


                String[] params = command.split(" ");
                File f = new File(params[params.length - 1]);
                String spamFile = "/Users/johan.risch/Documents/Exjobb/jsStream/streamDumps/2015-06-15_1150.txt";
//                int topics = Integer.parseInt(params[1])
//                int topics = Integer.parseInt(params[1])
                FakeTwitterStreamSupplierWithEvalTestSet supplier =
                        new FakeTwitterStreamSupplierWithEvalTestSet(params[params.length - 1], spamFile, spamLevel);
                supplier.setup();
                Scanner s = new Scanner(f);
                int[] failed = new int[params.length - 2];
                int[] correct = new int[params.length - 2];
                int[] incorrect = new int[params.length - 2];

                int[] Tp = new int[params.length - 2];
                int[] Fp = new int[params.length - 2];
                int[] Tn = new int[params.length - 2];
                int[] Fn = new int[params.length - 2];


                int[][] distribution = new int[params.length - 2][params.length - 2];
                String[] cats = new String[params.length - 2];
                HashMap<String, Integer> catIndex = new HashMap<>();
                for (int i = 0; i < cats.length; i++) {
                    catIndex.put(params[i + 1], i);
                    cats[i] = params[i + 1];
                }
                String[] realCats = new String[]{"president", "espn", "techcrunch", "charitywater"};

                for (int i = 0; i < data.length; i++) {
                    data[i] = new ArrayList<>();
                }
                String tweet = null;
                while ((tweet = supplier.getNext(0)) != null) {
                    String[] split = tweet.split(";;;");
                    String cat = split[0].replace("status_@", "").replace(".txt", "").toLowerCase();
                    tweet = split[1].toLowerCase()
                            .replaceAll("-", " ")
                            .replaceAll("[^a-z ]", "")
                            .replaceAll(" +", " ");
                    //System.out.print(tweet+" got ");


                    int result = categorize(worker.getResult(), tweet, false);
                    if (catIndex.get(cat) != null) {
                        distribution[result][catIndex.get(cat)]++;
                        data[result].add(tweet);
                    }

                    if (result < 0) {
                        failed[catIndex.get(cat)]++;
                    } else if (cats[result].equalsIgnoreCase(cat)) {
                        correct[result]++;
                        Tp[catIndex.get(cats[result])]++;
                        for (int i = 0; i < realCats.length; i++) {
                            if (realCats[i].equalsIgnoreCase(cats[result]))
                                continue;
                            if (catIndex.containsKey(realCats[i])) {
                                Tn[catIndex.get(realCats[i])]++;
                            }
                        }
                    } else {
                        if (catIndex.containsKey(cat)) {
                            incorrect[catIndex.get(cat)]++;
                            Fn[catIndex.get(cat)]++;
                        }

                        Fp[catIndex.get(cats[result])]++;


                    }

                }
                System.out.println(String.format("%-25s%-15s%-15s%-10s%-10s%-10s%-10s%-25s%-25s%-25s",
                        "topic", "purity", "main topic", "TP", "TN", "FM", "FN", "precision", "recall", "fmeasure"));
                int[] total = new int[3];
                for (int i = 0; i < cats.length; i++) {
                    int[] dist = distribution[i];
                    int totalT = 0;
                    int maxIndex = 0;
                    for (int j = 0; j < dist.length; j++) {
                        if (dist[j] > dist[maxIndex])
                            maxIndex = j;
                        totalT += dist[j];
                    }

                    total[0] += correct[i];
                    total[1] += incorrect[i];
                    total[2] += failed[i];
                    double precision = (double) Tp[i] / (double) (Tp[i] + Fp[i]);
                    double recall = (double) Tp[i] / (double) (Tp[i] + Fn[i]);

                    String print = String.format("%-25s%-15s%-15s%-10s%-10s%-10s%-10s%-25s%-25s%-25s",
                            cats[i],

                            (float) dist[maxIndex] / (float) totalT,
                            cats[maxIndex],
                            Tp[i], Fp[i], Tn[i], Fn[i], precision, recall, 2 * precision * recall / (precision + recall));

                    System.out.println(print);
                }
                System.out.println("---------------------------------------------------------------------------------------------------------");
                String print = String.format("%-25s%-15s%-15s%-15s%-15s",
                        "Total",
                        total[0], total[1], total[2], (float) total[0] / (float) (total[0] + total[1] + total[2]));
                System.out.println(print);
            }
            if (command.startsWith("search")) {
                long start = System.currentTimeMillis();
                String searchTerm = command.substring(command.indexOf(' '));
                String tweet = searchTerm.toLowerCase()
                        .replaceAll("-", " ")
                        .replaceAll("[^a-z ]", "")
                        .replaceAll(" +", " ");
                int result = categorize(worker.getResult(), tweet, false);
                if (result >= 0 && result < data.length) {
                    int min = Integer.MAX_VALUE;
                    String minTweet = "";
                    for (String s : data[result]) {
                        int distance = distance(tweet, s);
                        if (distance < min) {
                            min = distance;
                            minTweet = s;
                        }
                    }
                    long time = System.currentTimeMillis() - start;
                    System.out.println(String.format("Found tweet: \"%s\" in %s with LDA", minTweet, time));
                    start = System.currentTimeMillis();
                    min = Integer.MAX_VALUE;
                    minTweet = "";
                    for (ArrayList<String> al : data) {
                        for (String s : al) {
                            int distance = distance(tweet, s);
                            if (distance < min) {
                                min = distance;
                                minTweet = s;
                            }
                        }
                    }
                    time = System.currentTimeMillis() - start;
                    System.out.println(String.format("Found tweet: \"%s\" in %s with bruteforce", minTweet, time));
                }

            }
        }
    }

    public static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }


    private static long sDelta;

    private static int minFollowersCount = 500;
    public static englishStemmer stemmer;


    private static class Worker implements Runnable {
        private final StreamSupplier mSupplier;
        private OnlineLDA lda;
        private Result result;
        private boolean catTweets = true;
        private boolean running = true;
        private HashMap<Integer, LinkedList<String>> categorizedTweets = new HashMap<>();
        public int[] topicCount = new int[K + 1];
        private long tweetsSeen = 0;
        private long temp = 0;
        private RunningStat runningStat;

        public Worker(StreamSupplier supplier) {
            this.mSupplier = supplier;
        }

        public synchronized boolean shouldCat() {
            return catTweets;
        }

        public synchronized void setShouldCat(boolean b) {
            catTweets = b;
        }

        public synchronized void addTweetToTopic(int topic, String tweet) {
            if (!categorizedTweets.containsKey(topic)) {
                categorizedTweets.put(topic, new LinkedList<String>());
            }
            if (categorizedTweets.get(topic).size() > 20) {
                categorizedTweets.get(topic).removeLast();
            }
            categorizedTweets.get(topic).add(tweet);
            if (topic < topicCount.length && topic >= 0) {

                topicCount[topic]++;
                synchronized (this) {
                    tweetsSeen++;
                }
            }
        }

        public synchronized HashMap<Integer, LinkedList<String>> getCategorized() {
            return categorizedTweets;
        }

        public synchronized int[] getTopicCount() {
            return topicCount;
        }

        public synchronized long getTweetsSeen() {
            return tweetsSeen;
        }

        public synchronized void setResult(Result res) {
            if (this.result == null) {
                System.out.println("First results are in...");
                System.out.println("p,c,ap,stdevp");
            }
            this.result = res;
        }

        public synchronized Result getResult() {
            return result;
        }


        private synchronized boolean isRunning() {
            return running;
        }

        private synchronized void serRunning(boolean running) {
            this.running = running;
        }

        boolean firstBatch = true;

        @Override
        public void run() {
            HashSet<String> stopWords = new HashSet<>();
            for (String s : Main.stop_words) {
                stopWords.add(s);
            }

            try {
                double averagePerplexity = 0;
                double oldAveragePerplexity = 0;
                runningStat = new RunningStat();
                // Create an appropriately sized blocking queue

                ArrayList<Double> perplexity = new ArrayList<>();
                ArrayList<Long> count = new ArrayList<>();
                Main.stemmer = new englishStemmer();


                Vocabulary vocabulary = new PlainVocabulary(dictPath, 4, stemmer);
                lda = new OnlineLDA(vocabulary.size(), K, D, alpha, eta, tau, kappa);
                mSupplier.setup();
                List<String> tweets = new LinkedList<>();
                int bs = batchSize;
                batchSize = 1000;
                while (isRunning()) {
                    //  System.out.println("Running");
                    long start = System.currentTimeMillis();
                    tweets.clear();
                    while (tweets.size() < batchSize) {
                        batchSize = bs;
                        String message = mSupplier.getNext(minFollowersCount);

                        if (message != null) {

                            tweets.add(message);
                            temp++;
                        } else {
                            serRunning(false);
                            break;
                        }


                    }

                    Documents documents = new Documents(tweets, vocabulary, stemmer, new TwitterTokenizer());
                    Result res = lda.workOn(documents);

                    setResult(res);
                    if (shouldCat()) {
                        for (String tweet : tweets) {
                            int topic = categorize(getResult(), tweet, false);
                            addTweetToTopic(topic, tweet);

                        }
                    } else {
                        runningStat.put(res.perplexity);
                        System.out.println("" + res.perplexity + "," + temp + "," + runningStat.getAverage() + "," + runningStat.getStandardDeviation());

//                        perplexity.add(res.perplexity);
//                        count.add(temp);
                    }
                    sDelta = System.currentTimeMillis() - start;
                    if (temp >= D) {
                        break;
                    }

                }
                System.out.println("AVG: " + runningStat.getAverage() + ", stDev: " + runningStat.getStandardDeviation());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int categorize(Result res, String text, boolean print) {
//        long start = System.currentTimeMillis();
        double maxProbability = Double.MAX_VALUE;
        int chosenTopic = -1;
        String[] tokens = text.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            stemmer.setCurrent(tokens[i]);
            stemmer.stem();
            tokens[i] = stemmer.getCurrent();
        }
        HashMap<String, Integer> frqTable = frequencyTable(tokens);


        int numTopics = res.lambda.getNumberOfRows();
        int numTerms = Math.min(Result.NUMBER_OF_TOKENS, res.lambda.getNumberOfColumns());
        for (int k = 0; k < numTopics; ++k) {
            double logProbability = 1;
            risch.online.matrix.Vector termScores = res.lambda.getRow(k);
            Set<String> tokenSet = frqTable.keySet();
            Collection<Tuple> tuples = res.getTermsForTweets(termScores, numTerms, tokenSet);

            for (String s : tokenSet) {

                for (Tuple tuple : tuples) {
                    if (tuple.equalsW(s, res.documents)) {
                        if (print)
                            System.out.println(s + ": (" + (float) frqTable.get(s) / tokenSet.size() + "-" + tuple.value + "=" + Math.abs((float) frqTable.get(s) / tokenSet.size() - tuple.value) + ")");
                        logProbability += Math.abs((float) frqTable.get(s) / tokenSet.size() - tuple.value);
                    }
                }
            }
            if (logProbability < maxProbability) {
                maxProbability = logProbability;
                chosenTopic = k;
            }
        }
        if (print) {
            String topicS = topicsMap.containsKey("Topic " + chosenTopic) ? topicsMap.get("Topic " + chosenTopic) : "Topic " + chosenTopic;
            System.out.println(topicS + " with a probability of " + maxProbability);
        }
//        if (maxProbability < 0.05) {
//            return -1;
//        }
        return chosenTopic;

    }

    private static HashMap<String, Integer> frequencyTable(String[] tokens) {
        HashMap<String, Integer> ret = new HashMap<>();
        for (String s : tokens) {
            if (ret.containsKey(s)) {
                ret.put(s, ret.get(s) + 1);
            } else {
                ret.put(s, 1);
            }
        }
        return ret;
    }


    public static void setVal(String command) {
        String[] keyValue = command.split("=");
        switch (keyValue[0]) {
            case "D":
                D = Integer.parseInt(keyValue[1]);
                break;
            case "K":
                K = Integer.parseInt(keyValue[1]);
                worker.topicCount = new int[K + 1];
                break;
            case "batchSize":
                batchSize = Integer.parseInt(keyValue[1]);
                break;
            case "tau":
                tau = Double.parseDouble(keyValue[1]);
                break;
            case "kappa":
                kappa = Double.parseDouble(keyValue[1]);
                break;
            case "alpha":
                alpha = Double.parseDouble(keyValue[1]);
                break;
            case "eta":
                eta = Double.parseDouble(keyValue[1]);
                break;
            case "minFollowers":
                minFollowersCount = Integer.parseInt(keyValue[1]);
                break;
            case "cat":
                worker.setShouldCat(Boolean.parseBoolean(keyValue[1]));
                break;
            case "spamLevel":
                spamLevel = Float.parseFloat(keyValue[1]);
                break;

        }
        System.out.println("Current values");
        System.out.println("D = " + D);
        System.out.println("K = " + K);
        System.out.println("batchSize = " + batchSize);
        System.out.println("tau = " + tau);
        System.out.println("kappa = " + kappa);
        System.out.println("alpha = " + alpha);
        System.out.println("eta = " + eta);
        System.out.println("minFollowers = " + minFollowersCount);
        System.out.println("cat = " + worker.shouldCat());
        System.out.println("spamLevel = " + spamLevel);

    }


    public static String[] stop_words = new String[]{
            "@espn",
            "@techcrunch",
            "@president",
            "@charitywater",
            "espn",
            "techcrunch",
            "president",
            "charitywater",
            "presid",
            "obama",
            "today",
            "years",
            "rt",
            "http",
            "a",
            "able",
            "about",
            "above",
            "abroad",
            "according",
            "accordingly",
            "across",
            "actually",
            "adj",
            "after",
            "afterwards",
            "again",
            "against",
            "ago",
            "ahead",
            "aint",
            "all",
            "allow",
            "allows",
            "almost",
            "alone",
            "along",
            "alongside",
            "already",
            "also",
            "although",
            "always",
            "am",
            "amid",
            "amidst",
            "among",
            "amongst",
            "an",
            "and",
            "another",
            "any",
            "anybody",
            "anyhow",
            "anyone",
            "anything",
            "anyway",
            "anyways",
            "anywhere",
            "apart",
            "appear",
            "appreciate",
            "appropriate",
            "are",
            "arent",
            "around",
            "as",
            "as",
            "aside",
            "ask",
            "asking",
            "associated",
            "at",
            "available",
            "away",
            "awfully",
            "b",
            "back",
            "backward",
            "backwards",
            "be",
            "became",
            "because",
            "become",
            "becomes",
            "becoming",
            "been",
            "before",
            "beforehand",
            "begin",
            "behind",
            "being",
            "believe",
            "below",
            "beside",
            "besides",
            "best",
            "better",
            "between",
            "beyond",
            "both",
            "brief",
            "but",
            "by",
            "c",
            "came",
            "can",
            "cannot",
            "cant",
            "cant",
            "caption",
            "cause",
            "causes",
            "certain",
            "certainly",
            "changes",
            "clearly",
            "cmon",
            "co",
            "co.",
            "com",
            "come",
            "comes",
            "concerning",
            "consequently",
            "consider",
            "considering",
            "contain",
            "containing",
            "contains",
            "corresponding",
            "could",
            "couldnt",
            "course",
            "cs",
            "currently",
            "d",
            "dare",
            "darent",
            "definitely",
            "described",
            "despite",
            "did",
            "didnt",
            "different",
            "directly",
            "do",
            "does",
            "doesnt",
            "doing",
            "done",
            "dont",
            "down",
            "downwards",
            "during",
            "e",
            "each",
            "edu",
            "eg",
            "eight",
            "eighty",
            "either",
            "else",
            "elsewhere",
            "end",
            "ending",
            "enough",
            "entirely",
            "especially",
            "et",
            "etc",
            "even",
            "ever",
            "evermore",
            "every",
            "everybody",
            "everyone",
            "everything",
            "everywhere",
            "ex",
            "exactly",
            "example",
            "except",
            "f",
            "fairly",
            "far",
            "farther",
            "few",
            "fewer",
            "fifth",
            "first",
            "five",
            "followed",
            "following",
            "follows",
            "for",
            "forever",
            "former",
            "formerly",
            "forth",
            "forward",
            "found",
            "four",
            "from",
            "further",
            "furthermore",
            "g",
            "get",
            "gets",
            "getting",
            "given",
            "gives",
            "go",
            "goes",
            "going",
            "gone",
            "got",
            "gotten",
            "greetings",
            "h",
            "had",
            "hadnt",
            "half",
            "happens",
            "hardly",
            "has",
            "hasnt",
            "have",
            "havent",
            "having",
            "he",
            "hed",
            "hell",
            "hello",
            "help",
            "hence",
            "her",
            "here",
            "hereafter",
            "hereby",
            "herein",
            "heres",
            "hereupon",
            "hers",
            "herself",
            "hes",
            "hi",
            "him",
            "himself",
            "his",
            "hither",
            "hopefully",
            "how",
            "howbeit",
            "however",
            "hundred",
            "i",
            "id",
            "ie",
            "if",
            "ignored",
            "ill",
            "im",
            "immediate",
            "in",
            "inasmuch",
            "inc",
            "inc.",
            "indeed",
            "indicate",
            "indicated",
            "indicates",
            "inner",
            "inside",
            "insofar",
            "instead",
            "into",
            "inward",
            "is",
            "isnt",
            "it",
            "itd",
            "itll",
            "its",
            "its",
            "itself",
            "ive",
            "j",
            "just",
            "k",
            "keep",
            "keeps",
            "kept",
            "know",
            "known",
            "knows",
            "l",
            "last",
            "lately",
            "later",
            "latter",
            "latterly",
            "least",
            "less",
            "lest",
            "let",
            "lets",
            "like",
            "liked",
            "likely",
            "likewise",
            "little",
            "look",
            "looking",
            "looks",
            "low",
            "lower",
            "ltd",
            "m",
            "made",
            "mainly",
            "make",
            "makes",
            "man",
            "many",
            "may",
            "maybe",
            "maynt",
            "me",
            "mean",
            "meantime",
            "meanwhile",
            "merely",
            "might",
            "mightnt",
            "mine",
            "minus",
            "miss",
            "more",
            "moreover",
            "most",
            "mostly",
            "mr",
            "mrs",
            "much",
            "must",
            "mustnt",
            "my",
            "myself",
            "n",
            "name",
            "namely",
            "nd",
            "near",
            "nearly",
            "necessary",
            "need",
            "neednt",
            "needs",
            "neither",
            "never",
            "neverf",
            "neverless",
            "nevertheless",
            "new",
            "next",
            "nine",
            "ninety",
            "no",
            "nobody",
            "non",
            "none",
            "nonetheless",
            "noone",
            "no-one",
            "nor",
            "normally",
            "not",
            "nothing",
            "notwithstanding",
            "novel",
            "now",
            "nowhere",
            "o",
            "obviously",
            "of",
            "off",
            "often",
            "oh",
            "ok",
            "okay",
            "old",
            "on",
            "once",
            "one",
            "ones",
            "ones",
            "only",
            "onto",
            "opposite",
            "or",
            "other",
            "others",
            "otherwise",
            "ought",
            "oughtnt",
            "our",
            "ours",
            "ourselves",
            "out",
            "outside",
            "over",
            "overall",
            "own",
            "p",
            "particular",
            "particularly",
            "past",
            "per",
            "perhaps",
            "placed",
            "please",
            "plus",
            "possible",
            "presumably",
            "probably",
            "provided",
            "provides",
            "q",
            "que",
            "quite",
            "qv",
            "r",
            "rather",
            "rd",
            "re",
            "really",
            "reasonably",
            "recent",
            "recently",
            "regarding",
            "regardless",
            "regards",
            "relatively",
            "respectively",
            "right",
            "round",
            "s",
            "said",
            "same",
            "saw",
            "say",
            "saying",
            "says",
            "second",
            "secondly",
            "see",
            "seeing",
            "seem",
            "seemed",
            "seeming",
            "seems",
            "seen",
            "self",
            "selves",
            "sensible",
            "sent",
            "serious",
            "seriously",
            "seven",
            "several",
            "shall",
            "shant",
            "she",
            "shed",
            "shell",
            "shes",
            "should",
            "shouldnt",
            "since",
            "six",
            "so",
            "some",
            "somebody",
            "someday",
            "somehow",
            "someone",
            "something",
            "sometime",
            "sometimes",
            "somewhat",
            "somewhere",
            "soon",
            "sorry",
            "specified",
            "specify",
            "specifying",
            "still",
            "sub",
            "such",
            "sup",
            "sure",
            "t",
            "take",
            "taken",
            "taking",
            "tell",
            "tends",
            "th",
            "than",
            "thank",
            "thanks",
            "thanx",
            "that",
            "thatll",
            "thats",
            "thats",
            "thatve",
            "the",
            "their",
            "theirs",
            "them",
            "themselves",
            "then",
            "thence",
            "there",
            "thereafter",
            "thereby",
            "thered",
            "therefore",
            "therein",
            "therell",
            "therere",
            "theres",
            "theres",
            "thereupon",
            "thereve",
            "these",
            "they",
            "theyd",
            "theyll",
            "theyre",
            "theyve",
            "thing",
            "things",
            "think",
            "third",
            "thirty",
            "this",
            "thorough",
            "thoroughly",
            "those",
            "though",
            "three",
            "through",
            "throughout",
            "thru",
            "thus",
            "till",
            "to",
            "together",
            "too",
            "took",
            "toward",
            "towards",
            "tried",
            "tries",
            "truly",
            "try",
            "trying",
            "ts",
            "twice",
            "two",
            "u",
            "un",
            "under",
            "underneath",
            "undoing",
            "unfortunately",
            "unless",
            "unlike",
            "unlikely",
            "until",
            "unto",
            "up",
            "upon",
            "upwards",
            "us",
            "use",
            "used",
            "useful",
            "uses",
            "using",
            "usually",
            "v",
            "value",
            "various",
            "versus",
            "very",
            "via",
            "viz",
            "vs",
            "w",
            "want",
            "wants",
            "was",
            "wasnt",
            "way",
            "we",
            "wed",
            "welcome",
            "well",
            "well",
            "went",
            "were",
            "were",
            "werent",
            "weve",
            "what",
            "whatever",
            "whatll",
            "whats",
            "whatve",
            "when",
            "whence",
            "whenever",
            "where",
            "whereafter",
            "whereas",
            "whereby",
            "wherein",
            "wheres",
            "whereupon",
            "wherever",
            "whether",
            "which",
            "whichever",
            "while",
            "whilst",
            "whither",
            "who",
            "whod",
            "whoever",
            "whole",
            "wholl",
            "whom",
            "whomever",
            "whos",
            "whose",
            "why",
            "will",
            "willing",
            "wish",
            "with",
            "within",
            "without",
            "wonder",
            "wont",
            "would",
            "wouldnt",
            "x",
            "y",
            "yes",
            "yet",
            "you",
            "youd",
            "youll",
            "your",
            "youre",
            "yours",
            "yourself",
            "yourselves",
            "youve",
            "z",
            "zero"
    };


}
