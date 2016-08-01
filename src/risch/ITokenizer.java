package risch;

import java.util.List;

/**
 * Created by johan.risch on 07/09/15.
 */
public interface ITokenizer {
    List<String> extractTokens(String doc);
}
