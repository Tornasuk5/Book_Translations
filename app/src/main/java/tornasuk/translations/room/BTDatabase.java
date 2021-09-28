package tornasuk.translations.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import tornasuk.translations.classes.Translation;

/**
 * Instancia de la base de datos Room destinada a cargar los puntos cuando no haya conexión a internet
 * @author Tornasuk
 */
@Database(entities = {Translation.class}, version = 1, exportSchema = false)
public abstract class BTDatabase extends RoomDatabase {

    public abstract TranslationDAO translationDAO();

    private static volatile BTDatabase instance;

    static BTDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (BTDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            BTDatabase.class, "book_translations_database")
                            .build();
                }
            }
        }
        return instance;
    }
}

