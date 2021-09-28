package tornasuk.translations.classes;

import java.util.ArrayList;

public class Novel {
    private String novelName;
    private ArrayList<Volume> volumes;
    private int translationsPercent;
    private int numStartedVolumes;

    public Novel(String novelName, ArrayList<Volume> volumes) {
        this.novelName = novelName;
        this.volumes = volumes;
    }

    public Novel(String novelName) {
        this.novelName = novelName;
    }

    public Novel() {
    }

    public String getNovelName() {
        return novelName;
    }

    public void setNovelName(String novelName) {
        this.novelName = novelName;
    }

    public ArrayList<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(ArrayList<Volume> volumes) {
        this.volumes = volumes;
    }

    public int getTranslationsPercent() {
        return translationsPercent;
    }

    public void setTranslationsPercent(int translationsPercent) {
        this.translationsPercent = translationsPercent;
    }

    public int getNumStartedVolumes() {
        return numStartedVolumes;
    }

    public void setNumStartedVolumes(int numStartedVolumes) {
        this.numStartedVolumes = numStartedVolumes;
    }
}
