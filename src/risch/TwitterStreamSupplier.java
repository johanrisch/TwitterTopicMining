package risch;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by johan.risch on 09/06/15.
 */
public class TwitterStreamSupplier implements StreamSupplier {
    private static String consumerKey = "J5ZcsmHaRjOio57InGShpRePS";
    private static String consumerSecret = "A8cS00FlBB1b9Fv0G1ekDd3daIizcWigJo55Hcp3FQ1epiMf95";
    private static String token = "718801825-4iqcfpl8rWRvzkaCKC5mR32xuRrImd45ClteyT5B";
    private static String secret = "7BIfCh90Dytkritw18Kfu6nB0dKuoZEqYDcxyHAnHkeXY";

    BlockingQueue<String> msgQueue;// = new LinkedBlockingQueue<String>(100000);
    BlockingQueue<Event> eventQueue;// = new LinkedBlockingQueue<Event>(1000);

    public TwitterStreamSupplier() {


    }

    @Override
    public void setup() {
        msgQueue = new LinkedBlockingQueue<String>(100000);
        eventQueue = new LinkedBlockingQueue<Event>(1000);
        Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
        StatusesSampleEndpoint hosebirdEndpoint = new StatusesSampleEndpoint();


        List<String> languages = new LinkedList<>();
        languages.add("en");
        hosebirdEndpoint.languages(languages);

        List<Location> locations = new LinkedList<>();
        locations.add(new Location(new Location.Coordinate(-180, -90), new Location.Coordinate(180, 90)));


        ClientBuilder builder = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .authentication(auth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue))
                .eventMessageQueue(eventQueue);
        Client hosebirdClient = builder.build();
        hosebirdClient.connect();
    }

    @Override
    public String getNext(int minFollowersCount) throws InterruptedException {
        String message = msgQueue.take();
        JSONObject jObj;
        jObj = new JSONObject(message);
        if (jObj.has("user")) {
            int followers = jObj.getJSONObject("user").getInt("followers_count");
            if (followers > minFollowersCount) {
                String text = jObj.getString("text");
                text = text.toLowerCase();
                text = text.replaceAll("http(s:|:)//[^ ]*", "");
                text = text.replaceAll("[^a-zA-Z @]", "");
                return text;

            }
        }
        return null;

    }
}
