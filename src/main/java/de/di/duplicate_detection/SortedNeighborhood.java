package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.AttrSimWeight;
import de.di.duplicate_detection.structures.Duplicate;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.Levenshtein;
import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public class SortedNeighborhood {

    // A Record class that stores the values of a record with its original index. This class helps to remember the
    // original index of a record when this record is being sorted.
    @Data
    @AllArgsConstructor
    private static class Record {
        private int index;
        private String[] values;
    }

    /**
     * Discovers all duplicates in the relation by running the Sorted Neighborhood Method once with every sortingKey.
     * Each run uses one of the specified sortingKeys for the sorting, the windowsSize for the windowing, and
     * the recordComparator for the similarity calculations. A pair of records is classified as a duplicate and the
     * corresponding record indexes are returned as a Duplicate object, if the similarity of the two records w.r.t.
     * the provided recordComparator is equal to or greater than the similarityThreshold.
     * @param relation The relation, in which duplicates should be detected.
     * @param sortingKeys The sorting keys that should be used; a sorting key corresponds to an attribute index, whose
     *                    lexicographical order should determine a sortation; every specificed sorting key korresponds
     *                    to one Sorted Neighborhood run and the union of all duplicates of all runs is the result of
     *                    the call.
     * @param windowSize The window size each Sorted Neighborhood run should use.
     * @param recordComparator The record comparator each Sorted Neighborhood run should use when comparing records.
     * @return The list of discovered duplicate pairs of all Sorted Neighborhood runs.
     */
    public Set<Duplicate> detectDuplicates(Relation relation, int[] sortingKeys, int windowSize, RecordComparator recordComparator) {
        Set<Duplicate> duplicates = new HashSet<>();

        Record[] records = new Record[relation.getRecords().length];
        for (int i = 0; i < relation.getRecords().length; i++)
            records[i] = new Record(i, relation.getRecords()[i]);

        for (int sortingKey : sortingKeys) {
            Arrays.sort(records, Comparator.comparing(r -> {
                String val = r.getValues()[sortingKey];
                return val == null ? "" : val;
            }));

            for (int i = 0; i < records.length; i++) {
                for (int j = 1; j < windowSize && i + j < records.length; j++) {
                    Record r1 = records[i];
                    Record r2 = records[i + j];
                    double sim = recordComparator.compare(r1.getValues(), r2.getValues());
                    if (recordComparator.isDuplicate(sim)) {
                        duplicates.add(new Duplicate(relation, Math.min(r1.getIndex(), r2.getIndex()), Math.max(r1.getIndex(), r2.getIndex())));
                    }
                }
            }
        }

        return duplicates;
    }

    /**
     * Suggests a RecordComparator instance based on the provided relation for duplicate detection purposes.
     * @param relation The relation a RecordComparator needs to be suggested for.
     * @return A RecordComparator instance for comparing records of the provided relation.
     */
    public static RecordComparator suggestRecordComparatorFor(Relation relation) {
        List<AttrSimWeight> attrSimWeights = new ArrayList<>(relation.getAttributes().length);
        double threshold = 0.0;

        Tokenizer tokenizer = new Tokenizer();
        Jaccard jaccard = new Jaccard(tokenizer, false);
        int numAttributes = relation.getAttributes().length;
        double weight = 1.0 / numAttributes;
        for (int i = 0; i < numAttributes; i++) {
            attrSimWeights.add(new AttrSimWeight(i, jaccard, weight));
        }
        threshold = 0.7;

        return new RecordComparator(attrSimWeights, threshold);
    }
}
