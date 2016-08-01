package risch.tokens;

/*
Copyright (c) 2013 miberk

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import risch.Main;
import risch.TrieST;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlainVocabulary implements Vocabulary {
    int idx = 0;
    final List<String> strings = new ArrayList<String>();
    final HashMap<Integer, String> idMap = new HashMap<>();
    final TrieST<Integer> stringTrie = new TrieST<>();

    public PlainVocabulary(Collection<String> strings) {
        this.strings.addAll(strings);
    }

    public PlainVocabulary(String path, int minCount, SnowballStemmer stemmer   ) throws IOException {
        HashSet<String> stopWords = new HashSet<>();
        for (String s : Main.stop_words) {
            stopWords.add(s);
        }
        int maxCount = 25000;

        Scanner scanner = new Scanner(new File(path));
        while (scanner.hasNextLine()) {
            String word = scanner.nextLine().trim();
            String[] words = word.split(":");
            word = words[0];
            stemmer.setCurrent(word);
            stemmer.stem();
            word = stemmer.getCurrent();
            if (!stopWords.contains(word)) {
                if (words.length > 1 && Integer.parseInt(words[1]) > minCount  && Integer.parseInt(words[1]) < maxCount&& !stringTrie.contains(word)) {
                    stringTrie.put(word, idx);

                    idMap.put(idx, word);
                    idx++;
                }
//            strings.add(scanner.nextLine().trim());
            } else {
                System.out.println("Removed:  " + word);
            }
        }
    }

    @Override
    public boolean contains(String token) {
        return stringTrie.contains(token);
    }

    @Override
    public int size() {
        return stringTrie.size();
    }

    @Override
    public int getId(String token) {
        Integer val = stringTrie.get(token);
        if (val == null) {
            throw new IllegalArgumentException();
        }
        return val;
//        for(int i=0; i< strings.size();++i){
//            if(strings.get(i).equals(token)){
//                return i;
//            }
//        }
        // throw new IllegalArgumentException();
    }

    @Override
    public String getToken(int id) {
        return idMap.get(id);

    }

    @Override
    public void remove(String s) {
        if (stringTrie.contains(s)) {
            int i = stringTrie.get(s);
            stringTrie.delete(s);
            idMap.remove(i);
        }
    }

}
