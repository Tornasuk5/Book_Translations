package tornasuk.translations;

public class Utils {
    public static String getThemeFromID(String id){
        String theme = "";
        String idTheme = id.split("-")[0];
        if(idTheme.equals(Constantes.ID_GENERAL)){
            theme = "General";
        } else if(idTheme.equals(Constantes.ID_CLANNAD)){
            theme = "Clannad";
        } else if(idTheme.contains(Constantes.ID_CLASSROOM)){
            theme = "Classroom of the Elite";
        } else if(idTheme.contains(Constantes.ID_OVERLORD)){
            theme = "Overlord";
        } else if(idTheme.contains(Constantes.ID_LOGHORIZON)){
            theme = "Log Horizon";
        } else if(idTheme.contains(Constantes.ID_NGNL)){
            theme = "No Game No Life";
        }
        return theme;
    }
}
