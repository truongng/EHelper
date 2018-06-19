package thksoft.pte_helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TrNguyen on 22/02/2018.
 */

public class StringSimilarity {
    public static float wordsSimilarity(String src, String des) {
        List<String> originWords = new ArrayList<String>();
        int mesure = 1;
        if (src.contains("/")) {
            mesure = src.split("/").length;
            src = src.replace("/", " ");
        }
        int correctWords = 0, total = 1;
        try {
            for (String word : src.split(" ")) {
                originWords.add(Utilities.getWordOnly(word));
            }
            total = originWords.size();
            for (String word : des.split(" ")) {
                if (originWords.contains(Utilities.getWordOnly(word))) {
                    correctWords++;
                    originWords.remove(word);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (float) correctWords * 100 * mesure / total;
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 0; /* both strings are zero length */
        }
    /* // If you have Apache Commons Text, you can use it to calculate the edit distance:
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) * 100 / (double) longerLength;
    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static void printSimilarity(String s, String t) {
        System.out.println(String.format(
                "%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
    }
}
