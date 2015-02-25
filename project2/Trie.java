import java.util.HashSet;
import java.util.ArrayList;
import java.lang.StringBuilder;

public class Trie {
    
    private HashSet<String> stopwords = new HashSet<String>();
    private Node root = new Node(null);
    public static int maxOccuranceCount = 1;
    public static int maxModificationCount = 1;
    
    public Trie(HashSet<String> stopwords) {
        this.stopwords = stopwords;
    }
    
    public String sanitizeQuery(String query) {
        query = query.toLowerCase().replaceAll("[']", "").trim();
        String[] parts = query.split(" ");
        int index = 0;
        for(index = 0; index < parts.length; index++) {
            if(!this.stopwords.contains(parts[index])) {
                break;
            }
        }
        StringBuilder builder = new StringBuilder();
        for(; index < parts.length; index++) {
            builder.append(parts[index] + " ");
        }
        return builder.toString().trim();
    }
    
    public void add(String q, int occ, int mod) {
        //~ System.out.println("Adding: " + q);
        this.root.add(q, 0, occ, mod);
    }
    
    public Node find(String q) {
        return this.root.find(q, 0);
    }
    
    public void printAll() {
        this.root.printAll(new StringBuilder());
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<String> strings = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        this.root.findAll(nodes, strings, sb);
        System.out.println(strings);
        
        Node n = this.root.find("pop", 0);
        assert n != null;
        if(n==null) System.out.println("crap");
        nodes = new ArrayList<Node>();
        strings = new ArrayList<String>();
        sb = new StringBuilder();
        sb.append("pop");
        n.findAll(nodes, strings, sb);
        System.out.println(strings);
    }
    
    public class Node {
        
        private int occuranceCount = 0;
        private int modificationCount = 0;
        private Node[] children = null;
        private Node parent = null;
        
        Node(Node parent) {
            this.parent = parent;
        }
        
        public int getOccuranceCount() {
            return this.occuranceCount;
        }
        
        public int getModificationCount() {
            return this.modificationCount;
        }
        
        public void add(String q, int index, int occ, int mod) {
            //~ System.out.println("Enter: " + q);
            if(index > q.length()-1) {
                //~ System.out.println("Added: " + q);
                this.occuranceCount += occ;
                this.modificationCount += mod;
                if(occ > maxOccuranceCount) {
                    maxOccuranceCount = occ;
                }
                if(mod > maxModificationCount) {
                    maxModificationCount = mod;
                }
                return;
            }
            if(children == null) children = new Node[27];
            int childIndex = whichChild(q, index);
            if(children[childIndex] == null) {
                children[childIndex] = new Node(this);
            }
            children[childIndex].add(q, index + 1, occ, mod);
        }
        
        public Node find(String q, int index) {
            //~ System.out.println("Index: "+index);
            if(index > q.length()-1) {
                if(this.occuranceCount > 0) {
                    return this;
                } else {
                    return null;
                }
            }
            
            int childIndex = whichChild(q, index);
            if(children[childIndex] == null) {
                return null;
            } else {
                return children[childIndex].find(q, index + 1);
            }
        }
        
        public void findAll(ArrayList<Node> foundSoFar, ArrayList<String> stringsSoFar, StringBuilder stringSoFar) {
            if(this.occuranceCount > 0) {
                foundSoFar.add(this);
                stringsSoFar.add(stringSoFar.toString());
            }
            if(this.children == null) {
                return;
            }
            for(int i = 0; i < this.children.length; i++) {
                if(this.children[i] != null) {
                    char childsChar = whichChildChar(i);
                    stringSoFar.append(childsChar);
                    this.children[i].findAll(foundSoFar, stringsSoFar, stringSoFar);
                    stringSoFar.deleteCharAt(stringSoFar.length() - 1);
                }
            }
        }
        
        public void printAll(StringBuilder sb) {
            if(this.occuranceCount > 0) {
                System.out.println(sb.toString());
            }
            if(this.children == null) {
                return;
            }
            for(int i = 0; i < this.children.length; i++) {
                if(this.children[i] != null) {
                    char childChar = whichChildChar(i);
                    sb.append(childChar);
                    this.children[i].printAll(sb);
                    sb.deleteCharAt(sb.length() - 1);
                }
            }
        }
        
        private char whichChildChar(int index) {
            char result = (char) (index + 96);
            if(result > 122 || result < 97) {
                result = ' ';
            }
            return result;
        }
        
        private int whichChild(String q, int index) {
            int childIndex = (int)q.charAt(index) - 96;
            if(childIndex <= 0 || childIndex >= 27) {
                childIndex = 0;
            }
            return childIndex;
        }
    }
}
