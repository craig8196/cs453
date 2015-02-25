
public class QuerySuggestion implements Comparable<QuerySuggestion>{
    public String suggestedQuery = "";
    public double rank = 0;
    
    public QuerySuggestion(String sq, double rank) {
        this.suggestedQuery = sq;
        this.rank = rank;
    }
    
    public String toString() {
        return suggestedQuery + ": " + rank;
    }
    
    public int compareTo(QuerySuggestion qs) {
        double diff = qs.rank - this.rank;
        if(diff < 0) {
            return -1;
        } else if(diff > 0) {
            return 1;
        } else {
            return this.suggestedQuery.compareTo(qs.suggestedQuery);
        }
    }
}
