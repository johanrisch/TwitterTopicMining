package risch;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by johan.risch on 07/09/15.
 */
public class DefaultTokenizer implements ITokenizer {
    @Override
    public List<String> extractTokens(String doc) {
        List<String> result = new ArrayList<String>();
        BreakIterator boundary = BreakIterator.getWordInstance( );
        boundary.setText(doc);
        int start = boundary.first();
        for (int end = boundary.next();
             end != BreakIterator.DONE;
             start = end, end = boundary.next()) {
            String s = doc.substring(start, end);
            if (s.trim().length() > 0) {
                result.add(s.toLowerCase( ));
            }
        }
        return result;
    }

}
