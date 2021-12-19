package tornasuk.translations.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.Queue;

import tornasuk.translations.classes.Translation;

/**
 * Clase para ejecutar instrucciones sobre la base de datos Room
 * @author Tornasuk
 */

@Dao
public interface TranslationDAO {

        /**
         * Consulta que devuelve las 9 últimas traducciones almacenadas en la base de datos Room
         * @return Lista de las últimas traducciones
         */
        @Query("SELECT * FROM translations")
        List<Translation> getLastTranslations();

        /**
         * Inserta las 9 últimas traducciones almacenadas en Firebase
         * @param translation Lista de traducciones procedentes de Firebase
         */
        @Delete
        void deleteTranslation(Translation translation);

        /**
         * Inserta las 9 últimas traducciones almacenadas en Firebase
         * @param translation Lista de traducciones procedentes de Firebase
         */
        @Insert
        void insertTranslation(Translation translation);

        /**
         * Actualiza las traducciones existentes en la base de datos Room
         * @param translation Lista de traducciones procedentes de Firebase
         */
        @Update
        void updateTranslation(Translation translation);

}
