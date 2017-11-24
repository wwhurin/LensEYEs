package kr.hs.emirim.wwhurin.lenseye;

/**
 * Created by oarum on 2017-11-22.
 */

public class Lense {

    String date;
    boolean used;
    String name;
    String  term;
    String disuse;
    String key;


    public Lense(){}


    public Lense(String date, boolean used, String name, String term, String disuse, String key) {
        this.date = date;
        this.used = used;
        this.name = name;
        this.term = term;
        this.disuse = disuse;
        this.key = key;
    }



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDisuse() {
        return disuse;
    }

    public void setDisuse(String disuse) {
        this.disuse = disuse;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
