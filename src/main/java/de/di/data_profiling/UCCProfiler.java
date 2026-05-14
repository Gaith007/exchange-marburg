package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;

import java.util.ArrayList;
import java.util.List;

public class UCCProfiler {

    /**
     * Discovers all minimal, non-trivial unique column combinations in the provided relation.
     * @param relation The relation that should be profiled for unique column combinations.
     * @return The list of all minimal, non-trivial unique column combinations in ths provided relation.
     */
    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        List<UCC> uniques = new ArrayList<>();
        List<PositionListIndex> currentNonUniques = new ArrayList<>();

        // Calculate all unary UCCs and unary non-UCCs
        for (int attribute = 0; attribute < numAttributes; attribute++) {
            AttributeList attributes = new AttributeList(attribute);
            PositionListIndex pli = new PositionListIndex(attributes, relation.getColumns()[attribute]);
            if (pli.isUnique())
                uniques.add(new UCC(relation, attributes));
            else
                currentNonUniques.add(pli);
        }

        List<PositionListIndex> previousLevel = currentNonUniques;
        while (!previousLevel.isEmpty()) {
            List<PositionListIndex> currentLevelNonUniques = new ArrayList<>();
            for (int i = 0; i < previousLevel.size(); i++) {
                PositionListIndex pli1 = previousLevel.get(i);
                for (int j = i + 1; j < previousLevel.size(); j++) {
                    PositionListIndex pli2 = previousLevel.get(j);
                    if (pli1.getAttributes().samePrefixAs(pli2.getAttributes())) {
                        AttributeList candidateAttrs = pli1.getAttributes().union(pli2.getAttributes());
                        
                        boolean minimal = true;
                        for (UCC ucc : uniques) {
                            if (candidateAttrs.supersetOf(ucc.getAttributeList())) {
                                minimal = false;
                                break;
                            }
                        }
                        
                        if (minimal) {
                            PositionListIndex intersectedPli = pli1.intersect(pli2);
                            if (intersectedPli.isUnique()) {
                                uniques.add(new UCC(relation, candidateAttrs));
                            } else {
                                currentLevelNonUniques.add(intersectedPli);
                            }
                        }
                    }
                }
            }
            previousLevel = currentLevelNonUniques;
        }

        return uniques;
    }
}
