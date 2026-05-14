package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.IND;

import java.util.*;
import java.util.stream.Collectors;

public class INDProfiler {

    /**
     * Discovers all non-trivial unary (and n-ary) inclusion dependencies in the provided relations.
     * @param relations The relations that should be profiled for inclusion dependencies.
     * @return The list of all non-trivial unary (and n-ary) inclusion dependencies in the provided relations.
     */
    public List<IND> profile(List<Relation> relations, boolean discoverNary) {
        List<IND> inclusionDependencies = new ArrayList<>();

        for (Relation lhsRel : relations) {
            String[][] lhsCols = lhsRel.getColumns();
            List<Set<String>> lhsSets = toColumnSets(lhsCols);

            for (Relation rhsRel : relations) {
                String[][] rhsCols = rhsRel.getColumns();
                List<Set<String>> rhsSets = toColumnSets(rhsCols);

                for (int i = 0; i < lhsCols.length; i++) {
                    for (int j = 0; j < rhsCols.length; j++) {
                        if (lhsRel.equals(rhsRel) && i == j) continue;
                        if (rhsSets.get(j).containsAll(lhsSets.get(i))) {
                            inclusionDependencies.add(new IND(lhsRel, i, rhsRel, j));
                        }
                    }
                }
            }
        }

        if (discoverNary)
            // Here, the lattice search would start if n-ary IND discovery would be supported.
            throw new RuntimeException("Sorry, n-ary IND discovery is not supported by this solution.");

        return inclusionDependencies;
    }

    private List<Set<String>> toColumnSets(String[][] columns) {
        return Arrays.stream(columns)
                .map(column -> new HashSet<>(new ArrayList<>(List.of(column))))
                .collect(Collectors.toList());
    }
}
