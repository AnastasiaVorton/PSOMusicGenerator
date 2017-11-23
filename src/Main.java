import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Nastya on 04.11.2017.
 */
public class Main {
    static String musicString;
    //IMPORTANT!!! Change the following path to the path on your computer in order for program to save the midi file
    static String musicPath = "D:\\Документы\\Fall17\\Intro to AI\\Assignment2\\"; //the path to save the music sample
    static int TONIC; //tonic of a current tonality
    static int TONE; //tone of the current tonality (major or minor)
    static int[] TONALITY = new int[8]; //the gamma of the current tonality
    static Random random = new Random(); //used for generating random values of notes in chords and in melody
    public static ChordParticle GBEST; //global best chord
    public static NoteParticle noteGBest; //global best note
    public static int MAXITERATION = 50;//swarm population
    public static ChordParticle[] Chords = new ChordParticle[16]; //final chords sequence
    public static int[] Melody = new int[32]; //final notes in the melody
    public static ArrayList<ChordParticle> particles; //chords swarm for single PSO
    public static ArrayList<NoteParticle> noteParticles; //melody swarm for single PSO
    final static double m = 0.5, c1 = 0.5, c2 = 0.5; //coefficients for velocity calculations
    static int currentChord = 0; //counter for chords generation
    static int currentNote = 0; //counter for notes generation
    static long startTime, endTime, totalTime;
    static int chordsTotalIterations, melodyTotalIterations;
    static String output;

    /**
     * Writes the stats to the "output.csv" file
     *
     * @param s      one row of the table
     * @param append used for appending the string to an existing file
     */
    public static void writeFile(String s, boolean append) {
        FileWriter writer;
        try {
            writer = new FileWriter(new File("output.csv"), append);
            writer.write(s);
            writer.close();
        } catch (IOException ex) {
            return;
        }

    }

    /**
     * Used to generate tonality (tonic, tone and gamma of the tonality)
     */
    public static void generateTonality() {
        TONIC = random.nextInt(12) + 48;//I decided the tonic to be in the first octave in order for chords not to be too high
        TONE = random.nextInt(2);
        if (TONE == 0) { //Major
            System.out.println("The tone is Major and the tonic is " + TONIC);
            TONALITY[0] = TONIC;
            TONALITY[1] = TONIC + 2;
            TONALITY[2] = TONALITY[1] + 2;
            TONALITY[3] = TONALITY[2] + 1;
            TONALITY[4] = TONALITY[3] + 2;
            TONALITY[5] = TONALITY[4] + 2;
            TONALITY[6] = TONALITY[5] + 2;
            TONALITY[7] = TONALITY[6] + 1;
        } else { //Minor
            System.out.println("The tone is Minor and the tonic is " + TONIC);
            TONALITY[0] = TONIC;
            TONALITY[1] = TONIC + 2;
            TONALITY[2] = TONALITY[1] + 1;
            TONALITY[3] = TONALITY[2] + 2;
            TONALITY[4] = TONALITY[3] + 2;
            TONALITY[5] = TONALITY[4] + 1;
            TONALITY[6] = TONALITY[5] + 2;
            TONALITY[7] = TONALITY[6] + 2;
        }
    }

    /**
     * Initialises a swarm of chord particles
     */
    public static void initializeChords() {
        particles = new ArrayList<>();
        GBEST = new ChordParticle();
        ChordParticle particle;
        for (int i = 0; i < MAXITERATION; i++) {
            particle = new ChordParticle();
            for (int j = 0; j < 3; j++) {
                particle.chord[j] = random.nextInt(15) + TONIC; //I decided to generate chords in the lower one and a half octaves to prevent too big jumps between them
            }
            particles.add(particle);
        }
    }

    /**
     * Initializes a swarm of notes for the melody
     */
    public static void initializeMelody() {
        noteParticles = new ArrayList<>();
        noteGBest = new NoteParticle();
        NoteParticle particle;
        for (int i = 0; i < MAXITERATION; i++) {
            particle = new NoteParticle();
            particle.note = random.nextInt(24) + TONIC;
            noteParticles.add(particle);
        }
    }

    /**
     * Calculates fitness of a chord
     */
    public static void calculateChordFitness() {
        //for each chord in the swarm
        for (ChordParticle particle : particles) {
            int fitness = 0;
            int currentFitness = particle.fitness;
            if (TONE == 0) { //major
                //if the first note of the chord is a tonic, a dominant or a subdominant:
                if (particle.chord[0] % 12 == TONIC % 12 || particle.chord[0] % 12 == (TONIC + 5) % 12 || particle.chord[0] % 12 == (TONIC + 7) % 12) {
                    fitness++;
                }
                //if the second note of the chord is 2 tones higher:
                if (particle.chord[1] == particle.chord[0] + 4) {
                    fitness++;
                }
                //if the third note of the chord is 1.5 tones higher:
                if (particle.chord[2] == particle.chord[1] + 3) {
                    fitness++;
                }
                //if current chord is the first or the last one and its a tonic chord:
                if (currentChord == 0 || currentChord == 15) {
                    if (particle.chord[0] % 12 == TONIC % 12 && particle.chord[1] == particle.chord[0] + 4 && particle.chord[2] == particle.chord[1] + 3) {
                        fitness++;
                    }
                } else {
                    fitness++;
                }
                particle.fitness = fitness;
                //if current fitness is better then the last one assign new values to the personal best:
                if (particle.fitness > currentFitness) {
                    System.arraycopy(particle.chord, 0, particle.PBEST, 0, 3);
                }
            } else { //minor
                //same steps as in major  tone despite the change in the intervals
                if (particle.chord[0] % 12 == TONIC % 12 || particle.chord[0] % 12 == (TONIC + 5) % 12 || particle.chord[0] % 12 == (TONIC + 7) % 12) {
                    fitness++;
                }
                if (particle.chord[1] == particle.chord[0] + 3) {
                    fitness++;
                }
                if (particle.chord[2] == particle.chord[1] + 4) {
                    fitness++;
                }
                if (currentChord == 0 || currentChord == 15) {
                    if (particle.chord[0] % 12 == TONIC % 12 && particle.chord[1] == particle.chord[0] + 3 && particle.chord[2] == particle.chord[1] + 4)
                        fitness++;
                } else {
                    fitness++;
                }
                particle.fitness = fitness;
                if (particle.fitness > currentFitness) {
                    System.arraycopy(particle.chord, 0, particle.PBEST, 0, 3);
                }
            }
        }
    }

    /**
     * Calculates fitness of each note particle in the swarm for melody generation
     */
    public static void calculateNoteFitness() {
        for (NoteParticle particle : noteParticles) {
            int fitness = 0;
            int currentFitness = particle.fitness;
            // if note that plays with the chord is one of the chords notes and higher than that note
            if (currentNote % 2 == 0 && ((particle.note % 12 == Chords[currentNote / 2].chord[0] % 12 && particle.note > Chords[currentNote / 2].chord[0])
                    || (particle.note % 12 == Chords[currentNote / 2].chord[1] % 12 && particle.note > Chords[currentNote / 2].chord[1])
                    || (particle.note % 12 == Chords[currentNote / 2].chord[2] % 12 && particle.note > Chords[currentNote / 2].chord[2]))) {
                fitness++;
            }
            //if note that plays asynchronous with the chord is in the tonality(one of the notes in the gamma of that tonality) and higher than the chord
            if (currentNote % 2 == 1) {
                for (int i = 0; i < TONALITY.length; i++) {
                    if (particle.note % 12 == TONALITY[i] % 12 && particle.note > Chords[currentNote / 2].chord[2]) {
                        fitness++;
                    }
                }
            }
            //if the last note in the melody is the tonic:
            if (currentNote == 30) {
                if (particle.note % 12 == Chords[15].chord[0] % 12) {
                    fitness++;
                }
            } else {
                fitness++;
            }
            particle.fitness = fitness;
            if (particle.fitness > currentFitness) {
                particle.PBEST = particle.note;
            }
        }
    }


    /**
     * Simulates PSO for chords generation
     */
    public static void ChordPSO() {
        chordsTotalIterations = 0; //counter
        for (int k = 0; k < 16; k++) {
            currentChord = k;
            initializeChords();
            while (GBEST.fitness != 4) {
                calculateChordFitness();
                chordsTotalIterations++;
                //for each particle in the swarm
                for (ChordParticle particle : particles) {
                    //if particle's fitness is higher than the global best:
                    if (particle.fitness > GBEST.fitness) {
                        GBEST = particle;
                    }
                    //stopping condition
                    if (GBEST.fitness == 4) {
                        break;
                    }
                    //calculating velocity
                    for (int i = 0; i < 3; i++) {
                        particle.velocity[i] = (int) (m * particle.velocity[i] + c1 * random.nextDouble() * (particle.PBEST[i] - particle.chord[i]) + c2 * random.nextDouble() * (GBEST.chord[i] - particle.chord[i]));
                    }
                    //updating chords
                    for (int j = 0; j < 3; j++) {
                        particle.chord[j] = Math.abs(particle.chord[j] + particle.velocity[j]) % 15 + TONIC;
                    }
                }
            }
            ChordParticle currentChord = new ChordParticle();
            System.arraycopy(GBEST.chord, 0, currentChord.chord, 0, 3);
            Chords[k] = currentChord;
        }
    }

    /**
     * Simulates PSO for melody generation
     */
    public static void MelodyPSO() {
        melodyTotalIterations = 0;
        for (int k = 0; k < 31; k++) {
            currentNote = k;
            initializeMelody();
            while (noteGBest.fitness != 2) {
                calculateNoteFitness();
                melodyTotalIterations++;
                for (NoteParticle particle : noteParticles) {
                    if (particle.fitness > noteGBest.fitness) {
                        noteGBest = particle;
                    }
                    if (noteGBest.fitness == 2) {
                        break;
                    }
                    //calculating velocity
                    particle.velocity = (int) (m * particle.velocity + c1 * random.nextDouble() * (particle.PBEST - particle.note) + c2 * random.nextDouble() * (noteGBest.note - particle.note));
                    //updating chords
                    particle.note = Math.abs(particle.note + particle.velocity) % 12 + TONIC + 12;
                }
            }
            Melody[k] = noteGBest.note;
        }
    }


    /**
     * Creates a midi file from the generated chords and melody
     *
     * @param recordNo
     * @param tempo
     */
    private static void createMidiFile(int recordNo, int tempo) // throws IOException, InvalidMidiDataException
    {
        String midiFileNameEnd = ".mid";
        Pattern pattern = new Pattern(musicString).setVoice(0).setInstrument("Piano").setTempo(tempo);
        try {
            MidiFileManager.savePatternToMidi(pattern, new File(musicPath + Integer.toString(recordNo) + midiFileNameEnd));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a string to be transformed into a midi file
     */
    public static void musicStringBuilder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 31; i++) {
            if (i % 2 == 0) {
                sb.append(Chords[i / 2].chord[0] + "qa50+" + Chords[i / 2].chord[1] + "qa50+" + Chords[i / 2].chord[2] + "qa50+" + Melody[i] + "i ");
            } else if (i == 30) {
                sb.append(Chords[i / 2].chord[0] + "qa50+" + Chords[i / 2].chord[1] + "qa50+" + Chords[i / 2].chord[2] + "qa50+" + Melody[i] + "q ");
            } else if (i == 14) {
                sb.append(Chords[i / 2].chord[0] + "qa50+" + Chords[i / 2].chord[1] + "qa50+" + Chords[i / 2].chord[2] + "qa50+" + Melody[i] + "q ");
            } else if (i == 15) {
                //the duration if the last chord and note in the 2nd bar is longer
                sb.append("0ia0 ");
            } else {
                sb.append(Melody[i] + "i ");
            }
        }
        musicString = sb.toString();
    }


    public static void main(String[] args) {
        writeFile("Time" + ';' + "Chord iterations" + ';' + "Melody iterations" + "\n", false);
        for (int r = 0; r < 16; r++) {
            Chords[r] = new ChordParticle();
        }
        startTime = System.currentTimeMillis();
        generateTonality();
        ChordPSO();
        MelodyPSO();
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        int totalIterations = chordsTotalIterations + melodyTotalIterations;
        output = totalTime + ";" + chordsTotalIterations + ";" + melodyTotalIterations + "\n";
        writeFile(output, true);
        int q = 0;
        for (ChordParticle p : Chords) {
            System.out.println(p.chord[0] + " " + p.chord[1] + " " + p.chord[2] + " " + Melody[q] + " " + Melody[q + 1]);
            q += 2;
        }
        musicStringBuilder();
        createMidiFile(random.nextInt(20), 100);
    }
}
