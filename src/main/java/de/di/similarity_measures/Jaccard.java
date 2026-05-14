package de.di.similarity_measures;

import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class Jaccard implements SimilarityMeasure {

    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;

    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;

    /**
     * Calculates the Jaccard similarity of the two input strings. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String string1, String string2) {
        string1 = (string1 == null) ? "" : string1;
        string2 = (string2 == null) ? "" : string2;

        String[] strings1 = this.tokenizer.tokenize(string1);
        String[] strings2 = this.tokenizer.tokenize(string2);
        return this.calculate(strings1, strings2);
    }

    /**
     * Calculates the Jaccard similarity of the two string lists. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String[] strings1, String[] strings2) {
        double jaccardSimilarity = 0;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        if (this.bagSemantics) {
            Map<String, Integer> counts1 = new HashMap<>();
            for (String s : strings1) counts1.put(s, counts1.getOrDefault(s, 0) + 1);
            Map<String, Integer> counts2 = new HashMap<>();
            for (String s : strings2) counts2.put(s, counts2.getOrDefault(s, 0) + 1);

            int intersectionSize = 0;
            Set<String> allKeys = new HashSet<>(counts1.keySet());
            allKeys.addAll(counts2.keySet());
            
            for (String key : allKeys) {
                int c1 = counts1.getOrDefault(key, 0);
                int c2 = counts2.getOrDefault(key, 0);
                intersectionSize += Math.min(c1, c2);
            }
            int totalSize = strings1.length + strings2.length;
            if (totalSize > 0) {
                jaccardSimilarity = (double) intersectionSize / totalSize;
            }
        } else {
            Set<String> set1 = new HashSet<>(Arrays.asList(strings1));
            Set<String> set2 = new HashSet<>(Arrays.asList(strings2));
            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);
            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);
            if (!union.isEmpty()) {
                jaccardSimilarity = (double) intersection.size() / union.size();
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return jaccardSimilarity;
    }
}
