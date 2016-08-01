package risch;

/**
 * Created by johan.risch on 09/06/15.
 */
public interface StreamSupplier {
    void setup();
    String getNext(int minFollowersCount) throws InterruptedException;
}
