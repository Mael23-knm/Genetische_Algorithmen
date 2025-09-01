package genetic_algorithm;
import Tile.Tile;
import Folding.Folding;
import utilities.ImageCreator;
import utilities.utilities;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;


public class GeneticAlgo {
    private Folding[] population;
    private Folding best_candidate;
    private Folding best_gen_candidate;
    private BufferedWriter bw;
    private final int sequenceLength;
    private int parameter_K = 2;
    private double mutation_factor;

    public GeneticAlgo(int populationSize, String sequence) {
        this.population = new Folding[populationSize];
        this.sequenceLength = sequence.length();
        for (int i = 0; i < populationSize; i++) {
            this.population[i] = new Folding(sequence);
        }
        this.mutation_factor = 1.0;

        try {
            new java.io.File("Logs").mkdirs(); // Ordner sicher anlegen
            this.bw = new BufferedWriter(new FileWriter("Logs/informations.csv", false));
            this.bw.write("generation\tavg_fitness\tbest_gen_fitness\tbest_so_far_fitness\thh_contacts\toverlaps\tmutation_rate\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void work(int number_of_generations, double expected_fitness, double crossoverRate, double mutationRate,
                     utilities.Selection selection_method, boolean dynamic_change) throws CloneNotSupportedException, IOException {

        int generation_number = 0;
        double start_mutation = mutationRate;   // Startwert
        double min_mutation = 0.01;             // Minimalwert (nur für dynamische Variante)

        while (generation_number < number_of_generations && this.evaluation() < expected_fitness) {

            // Fitness-proportional oder Turnierselektion
            if (selection_method == utilities.Selection.FITNESS) {
                this.population = this.select_candidates_fitness();
            } else if (selection_method == utilities.Selection.TOURNAMENT) {
                this.population = this.select_candidates_tournament();
            }

            // Mutationsrate festlegen
            double mutation_rate;
            if (dynamic_change) {
                // Dynamisch abhängig von Generation
                mutation_rate = start_mutation - ((start_mutation - min_mutation) *
                        (generation_number / (double) number_of_generations));
            } else {
                // Konstant: immer gleich
                mutation_rate = mutationRate;
            }

            // Logging
            this.write_log_line(generation_number, mutation_rate);

            // Crossover und Mutation
            this.one_point_crossover(crossoverRate);
            this.mutation(mutation_rate * this.mutation_factor);

            // Nur wenn dynamische Steuerung aktiv ist
            if (dynamic_change) {
                this.compute_mutation_factor(expected_fitness, mutation_rate);
            }

            // Debug-Ausgabe
            System.out.println("Generation " + generation_number);
            System.out.println("Best Known Candidate: " + this.best_candidate.fitness());
            System.out.println("Best Gen Candidate: " + this.best_gen_candidate.fitness());
            generation_number++;
        }

        try {
            if (bw != null) {
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compute_mutation_factor(double expected_fitness, double mutation_rate){
        this.mutation_factor = 1 + (1.0 - mutation_rate) * (this.evaluation() / expected_fitness);
    }

    private void one_point_crossover(double crossover_rate) throws CloneNotSupportedException {
        int numberOfPermutations = (int) (this.population.length * crossover_rate);
        Random rand = new Random();
        for(int i = 0; i < numberOfPermutations; i++){
            int first_index = rand.nextInt(population.length);
            Folding first = (Folding) this.population[first_index].clone();

            int second_index = rand.nextInt(population.length);
            while (second_index == first_index)
                second_index = rand.nextInt(population.length);

            Folding second = (Folding) this.population[second_index].clone();
            int point_separation = rand.nextInt(this.sequenceLength) + 1;

            Tile[] first_geno_part = first.cute_elements(0, point_separation);
            Tile[] second_geno_part = second.cute_elements(0, point_separation);

            first.compute_crossover(second_geno_part);
            second.compute_crossover(first_geno_part);

            population[first_index] = first;
            population[second_index] = second;
        }
    }


    private void mutation(double mutation_rate) {
        int numberOfMutations = (int) (this.population.length * mutation_rate);
        Random rand = new Random();
        for (int i = 0; i < numberOfMutations; i++) {
            int randomIndex = rand.nextInt(this.population.length);
            this.population[randomIndex].mutate(rand.nextInt(sequenceLength));
        }
    }



    private void write_log_line(int gen_number, double mutation_rate) throws IOException {
        this.bw.write(
                gen_number + "\t" +
                        String.format(Locale.GERMANY,"%f",this.evaluation()) + "\t" +
                       String.format(Locale.GERMANY,"%f", this.best_gen_candidate.fitness() )+ "\t" +
                        String.format(Locale.GERMANY,"%f", this.best_candidate.fitness() )+ "\t" +
                        this.best_candidate.getNumberBonds() + "\t" +
                        this.best_candidate.getNumberOverlaps() + "\t" +
                        String.format(Locale.GERMANY, "%f", mutation_rate) + "\n"
        );
    }

    public Folding getBestCandidate() {
        return this.best_candidate;
    }

    private double evaluation() {
        return sum_fitness() / this.population.length;
    }

    public Folding[] select_candidates_tournament() throws CloneNotSupportedException {
        Folding[] newPopulation = new Folding[this.population.length];
        Random rand = new Random();
        int counter = 0;

        if(this.parameter_K == 2){
            while (counter < this.population.length){

                int first = rand.nextInt(population.length);
                int second = rand.nextInt(population.length);

                double fitness1 = this.population[first].fitness();
                double fitness2 = this.population[second].fitness();

                double dv = rand.nextDouble(1.0);

                int max = first;
                int min = second;
                if(fitness2 > fitness1) {
                    max = second;
                    min = first;
                }

                if(dv < 0.25){
                    newPopulation[counter] = (Folding) this.population[min].clone();
                }else{
                    newPopulation[counter] = (Folding) this.population[max].clone();
                }
                counter++;
            }
        }else {

            while (counter < this.population.length) {

                Folding[] tournament_candidates = new Folding[parameter_K];
                for (int i = 0; i < parameter_K; i++) {
                    tournament_candidates[i] = this.population[rand.nextInt(this.population.length)];
                }
                newPopulation[counter] = (Folding) selectBestCandidates(tournament_candidates).clone();
                counter++;
            }
        }
        this.best_gen_candidate = (Folding) this.selectBestCandidates(newPopulation).clone();
        if(this.best_candidate == null || this.best_candidate.fitness() < this.best_gen_candidate.fitness()){
            this.best_candidate = (Folding) this.best_gen_candidate.clone();
        }

        return newPopulation;
    }

    public Folding[] select_candidates_fitness() throws CloneNotSupportedException {

        double[] relative_fitness = new double[this.population.length];

        double sum_fitness = this.sum_fitness();
        relative_fitness[0] = this.population[0].fitness() / sum_fitness;

        for(int i = 1; i < this.population.length; i++){
            relative_fitness[i] = relative_fitness[i - 1] + (this.population[i].fitness() / sum_fitness);
        }

        Folding[] newPopulation = new Folding[this.population.length];
        int counter = 0;
        while(counter < this.population.length){
            double random_value = Math.random();

            int folding_index =  this.getFoldingIndex(relative_fitness, random_value);
            newPopulation[counter] = (Folding) this.population[folding_index].clone();

            counter++;
        }

        this.best_gen_candidate = (Folding) this.selectBestCandidates(newPopulation).clone();
        if(this.best_candidate == null || this.best_candidate.fitness() < this.best_gen_candidate.fitness()){
            this.best_candidate = (Folding) this.best_gen_candidate.clone();
        }

        return newPopulation;
    }

    private Folding selectBestCandidates(Folding[] foldings) {
        int best_index = 0;
        double best_fitness = 0.0;
        for(int i = 0; i < foldings.length; i++ ){
            double actual_fitness = foldings[i].fitness();
            if(actual_fitness > best_fitness){
                best_fitness = actual_fitness;
                best_index = i;
            }
        }
        return foldings[best_index];
    }


    private int getFoldingIndex(double[] ranges, double value) {
        for (int i = 0; i < ranges.length; i++)
            if (ranges[i] >= value)
                return i;
        //This should never happen, but just in case
        return -1;
    }

    private double sum_fitness() {
        double sum = 0.0;
        for (Folding folding : this.population) {
            sum += folding.fitness();
        }
        return sum;
    }

    public void setParameter_K(int k){
        this.parameter_K = k;
    }

    public void print_images(){
        ImageCreator ic = new ImageCreator(2700, 2700, 150, 100, 200);
        for(int i = 0; i < this.population.length; i++)
            ic.createFoldingImage(this.population[i], String.valueOf(i));
    }
}
