package tornasuk.translations;

public class Utils {
    public static String getThemeFromID(String id){
        String theme = "";
        String idTheme = id.split("-")[0];
        switch(idTheme){
            case Constantes.ID_GENERAL:
                theme = "General";
                break;
            case Constantes.ID_CLANNAD:
                theme = "Clannad";
                break;
            case Constantes.ID_CLASSROOM:
                theme = "Classroom of the Elite";
                break;
            case Constantes.ID_OVERLORD:
                theme = "Overlord";
                break;
            case Constantes.ID_LOGHORIZON:
                theme = "Log Horizon";
                break;
            case Constantes.ID_NGNL:
                theme = "No Game No Life";
                break;
            case Constantes.ID_KOICHOCO:
                theme = "Koichoco";
                break;
        }
        return theme;
    }
}
