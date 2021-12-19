package tornasuk.translations.room;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tornasuk.translations.classes.Translation;
/**
 * Clase para inicializar y poder manejar la base de datos Room
 * @author Tornasuk
 */

public class TranslationRepository {
    private final TranslationDAO translationDAO;
    public final ExecutorService executorService;
    private List<Translation> lastTranslations;

    /**
     * Constructor de PointRepository
     * @param application Contexto para poder instanciar la base de datos Room
     */
    public TranslationRepository(Application application) {
        BTDatabase db = BTDatabase.getDatabase(application);
        executorService = Executors.newFixedThreadPool(2);
        translationDAO = db.translationDAO();
        lastTranslations = new ArrayList<>();
    }

    /**
     * Inserta por primera vez los puntos de Firebase en la base de datos Room
     * @param lastTranslation Lista de puntos procedentes de Firebase
     */
    public void insertLastTranslation(Translation lastTranslation) {
        executorService.execute(() -> {
            ArrayList<Translation> translations = (ArrayList<Translation>) translationDAO.getLastTranslations();
            if (translations.size() == 9)
                translationDAO.deleteTranslation(translations.get(0));
            translationDAO.insertTranslation(lastTranslation);
        });
    }

    /**
     * Actualiza los puntos de Firebase en la base de datos Room
     * @param lastTranslation Traducción procedentes de Firebase
     */
    public void updateTranslation(Translation lastTranslation) {
        translationDAO.updateTranslation(lastTranslation);
    }

    public void deleteTranslation(Translation lastTranslation) {
        translationDAO.deleteTranslation(lastTranslation);
    }

    /**
     * Devuelve los puntos almacenados en la base de datos Room
     * @return Lista de puntos almacenados en la base de datos Room
     */
    public List<Translation> getLastTranslations(){
        lastTranslations = translationDAO.getLastTranslations();
        return lastTranslations;
    }
}
