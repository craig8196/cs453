
public class Pair implements Comparable<Pair>{
    public Document doc = null;
    public double score = 0;
    
    public Pair(Document doc, double score) {
        this.doc = doc;
        this.score = score;
    }
    
    public int compareTo(Pair p) {
        double temp = p.score - this.score;
        if (temp < 0) return -1;
        else if (temp > 0) return 1;
        else return 0;
    }
}
