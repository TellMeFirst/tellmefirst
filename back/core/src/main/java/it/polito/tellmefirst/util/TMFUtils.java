/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2012 Federico Cairo, Giuseppe Futia, Federico Benedetto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.polito.tellmefirst.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class TMFUtils {

    static Log LOG = LogFactory.getLog(TMFUtils.class);

    public static Set<String> getStopWords(String stopwordsFilePath) {
        LOG.debug("[getStopWords] - BEGIN");
        ArrayList<String> stopWordsList = new ArrayList<String>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(stopwordsFilePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stopWordsList.add(line.trim());
            }
            bufferedReader.close();
        } catch (Exception e) {
            LOG.error("Could not read stopwords file at location: " + stopwordsFilePath);
        }
        Set<String> stopwordsSet = new HashSet<String>(stopWordsList);
        LOG.debug("[getStopWords] - END");
        return stopwordsSet;
    }

    // take a look at: http://www.lampos.net/sort-hashmap
    public static LinkedHashMap sortHashMapIntegers(HashMap passedMap) {
        LOG.debug("[sortHashMapIntegers] - BEGIN");
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.reverse(mapValues);
        Collections.sort(mapKeys);
        LinkedHashMap sortedMap = new LinkedHashMap();
        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();
            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();
                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((Integer) key, (Integer) val);
                    break;
                }
            }
        }
        LOG.debug("[sortHashMapIntegers] - END");
        return sortedMap;
    }


    public static int countWords(String in) {
        LOG.debug("[countWords] - BEGIN");
        String[] words = in.split(" ");
        LOG.debug("[countWords] - END");
        return words.length;
    }
}
