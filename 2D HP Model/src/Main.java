import genetic_algorithm.GeneticAlgo;
import utilities.ImageCreator;
import utilities.Examples;
import utilities.utilities;

public class Main {
    public static void main(String[] args) throws Exception {
        ImageCreator ic = new ImageCreator(2700, 2700, 150, 100, 200);

        // Abnahmeszenario: 100 Kandidaten
        GeneticAlgo ga = new GeneticAlgo(200, Examples.SEQ64);
        ga.setParameter_K(3);

        // 100 Generationen, fitnessproportional, KEIN Crossover & KEINE Mutation
        ga.work(300, 21.0, 0.8, 0.05, utilities.Selection.TOURNAMENT, true);

        ic.createFoldingImage(ga.getBestCandidate(), "best_candidate");
    }
}
