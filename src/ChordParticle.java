/**
 * Created by Nastya on 05.11.2017.
 */
public class ChordParticle  {
    public int[] chord;
    public int velocity[]; //how much data can be changed
    public int[] PBEST;
    public int fitness;

    public ChordParticle() {
        this.chord = new int[3];
        for (int i = 0; i < 3; i++){
            chord[i] = 0;
        }
        this.velocity = new int[3];
        for (int k = 0; k < 3; k++){
            chord[k] = 0;
        }
        this.PBEST = new int[3];
        for (int l = 0; l < 3; l++){
            chord[l] = 0;
        }
        fitness = 0;
    }

//    @Override
//    public int compareTo(ChordParticle o) {
//        return this.chord[0] > o.chord[0] && this.chord[1] > o.chord[1] && this.chord[2] > o.chord[2] ? this : o;
//    }
}
