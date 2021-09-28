package tornasuk.translations.classes;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;

public class Volume {
    private String nameVolumen;
    private String volImg;
    private ArrayList<Translation> translations;

    public Volume(String nameVolumen, String volImg, ArrayList<Translation> translations) {
        this.nameVolumen = nameVolumen;
        this.volImg = volImg;
        this.translations = translations;
    }

    public Volume(String nameVolumen, String volImg) {
        this.nameVolumen = nameVolumen;
        this.volImg = volImg;
    }

    public Volume(String nameVolumen) {
        this.nameVolumen = nameVolumen;
    }

    public Volume() {
    }

    @Exclude
    public String getNameVolumen() {
        return nameVolumen;
    }
    @Exclude
    public void setNameVolumen(String nameVolumen) {
        this.nameVolumen = nameVolumen;
    }

    public String getVolImg() {
        return volImg;
    }

    public void setVolImg(String volImg) {
        this.volImg = volImg;
    }

    @Exclude
    public ArrayList<Translation> getTranslations() {
        return translations;
    }
    @Exclude
    public void setTranslations(ArrayList<Translation> translations) {
        this.translations = translations;
    }
}
