package tornasuk.translations.classes;

import java.util.ArrayList;

public class Page {

    private int numPage;
    private ArrayList<Translation> translations;

    public Page(int numPage, ArrayList<Translation> translations) {
        this.numPage = numPage;
        this.translations = translations;
    }

    public Page(int numPage) {
        this.numPage = numPage;
    }

    public Page(){

    }

    public int getNumPage() {
        return numPage;
    }

    public void setNumPage(int numPage) {
        this.numPage = numPage;
    }

    public ArrayList<Translation> getTranslations() {
        return translations;
    }

    public void setTranslations(ArrayList<Translation> translations) {
        this.translations = translations;
    }
}
