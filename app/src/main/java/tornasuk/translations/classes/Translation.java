package tornasuk.translations.classes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Translations")
public class Translation {

    @NonNull
    @PrimaryKey
    private String id;

    private String word;
    private String wordTranslation;
    private String volume;
    private String novel;

    public Translation(@NonNull String id, String word, String wordTranslation, String volume, String novel) {
        this.id = id;
        this.word = word;
        this.wordTranslation = wordTranslation;
        this.volume = volume;
        this.novel = novel;
    }

    public Translation(@NonNull String id, String word, String wordTranslation){
        this.id = id;
        this.word = word;
        this.wordTranslation = wordTranslation;
    }

    public Translation(){
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWordTranslation() {
        return wordTranslation;
    }

    public void setWordTranslation(String wordTranslation) {
        this.wordTranslation = wordTranslation;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getNovel() {
        return novel;
    }

    public void setNovel(String novel) {
        this.novel = novel;
    }

    public String getNumId(){
        return this.id != null ? this.id.split("-")[1] : "0";
    }
}

