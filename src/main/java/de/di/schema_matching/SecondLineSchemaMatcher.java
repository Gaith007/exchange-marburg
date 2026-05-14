package de.di.schema_matching;

import de.di.schema_matching.structures.CorrespondenceMatrix;
import de.di.schema_matching.structures.SimilarityMatrix;

import java.util.Arrays;

public class SecondLineSchemaMatcher {

    /**
     * Translates the provided similarity matrix into a binary correspondence matrix by selecting possibly optimal
     * attribute correspondences from the similarities.
     * @param similarityMatrix A matrix of pair-wise attribute similarities.
     * @return A CorrespondenceMatrix of pair-wise attribute correspondences.
     */
    public CorrespondenceMatrix match(SimilarityMatrix similarityMatrix) {
        double[][] simMatrix = similarityMatrix.getMatrix();

        int n = simMatrix.length;
        if (n == 0) return new CorrespondenceMatrix(new int[0][0], similarityMatrix.getSourceRelation(), similarityMatrix.getTargetRelation());
        int m = simMatrix[0].length;
        
        int[] sourceAssignments = new int[n];
        Arrays.fill(sourceAssignments, -1);
        int[] targetAssignments = new int[m];
        Arrays.fill(targetAssignments, -1);
        
        int[][] sourcePrefs = new int[n][m];
        for (int i = 0; i < n; i++) {
            Integer[] indices = new Integer[m];
            for (int j = 0; j < m; j++) indices[j] = j;
            final int row = i;
            Arrays.sort(indices, (a, b) -> Double.compare(simMatrix[row][b], simMatrix[row][a]));
            for (int j = 0; j < m; j++) sourcePrefs[i][j] = indices[j];
        }
        
        int[] nextProposal = new int[n];
        boolean[] freeSource = new boolean[n];
        Arrays.fill(freeSource, true);
        int freeCount = n;
        
        while (freeCount > 0) {
            int s = -1;
            for (int i = 0; i < n; i++) {
                if (freeSource[i] && nextProposal[i] < m) {
                    s = i;
                    break;
                }
            }
            if (s == -1) break;
            
            int t = sourcePrefs[s][nextProposal[s]++];
            
            if (targetAssignments[t] == -1) {
                targetAssignments[t] = s;
                sourceAssignments[s] = t;
                freeSource[s] = false;
                freeCount--;
            } else {
                int sPrime = targetAssignments[t];
                if (simMatrix[s][t] > simMatrix[sPrime][t]) {
                    targetAssignments[t] = s;
                    sourceAssignments[s] = t;
                    freeSource[s] = false;
                    freeSource[sPrime] = true;
                }
            }
        }
        
        int[][] corrMatrix = assignmentArray2correlationMatrix(sourceAssignments, simMatrix);

        return new CorrespondenceMatrix(corrMatrix, similarityMatrix.getSourceRelation(), similarityMatrix.getTargetRelation());
    }

    /**
     * Translate an array of source assignments into a correlation matrix. For example, [0,3,2] maps 0->1, 1->3, 2->2
     * and, therefore, translates into [[1,0,0,0][0,0,0,1][0,0,1,0]].
     * @param sourceAssignments The list of source assignments.
     * @param simMatrix The original similarity matrix; just used to determine the number of source and target attributes.
     * @return The correlation matrix extracted form the source assignments.
     */
    private int[][] assignmentArray2correlationMatrix(int[] sourceAssignments, double[][] simMatrix) {
        int[][] corrMatrix = new int[simMatrix.length][];
        for (int i = 0; i < simMatrix.length; i++) {
            corrMatrix[i] = new int[simMatrix[i].length];
            for (int j = 0; j < simMatrix[i].length; j++)
                corrMatrix[i][j] = 0;
        }
        for (int i = 0; i < sourceAssignments.length; i++)
            if (sourceAssignments[i] >= 0)
                corrMatrix[i][sourceAssignments[i]] = 1;
        return corrMatrix;
    }
}