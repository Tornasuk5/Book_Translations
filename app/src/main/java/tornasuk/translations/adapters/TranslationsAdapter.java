package tornasuk.translations.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tornasuk.translations.classes.Translation;
import tornasuk.translations.Constantes;
import tornasuk.translations.R;
import tornasuk.translations.databinding.ListTranslationsBinding;

public class TranslationsAdapter extends RecyclerView.Adapter<TranslationsAdapter.TranslationViewHolder> implements Filterable {

    private final ArrayList<Translation> translations;
    private final ArrayList<Translation> translationsFilterList;
    private ArrayList<Translation> filteredList;
    private final Context context;
    private View.OnLongClickListener longListener;
    private View.OnClickListener clickListener;
    private String searchType;
    private String fragType;

    public TranslationsAdapter(Context context, List<Translation> translations2, String fragType) {
        this.context = context;
        this.translations = (ArrayList<Translation>) translations2;
        this.fragType = fragType;
        translationsFilterList = new ArrayList<>(translations);
    }

    @Override
    public int getItemCount() { // DEVUELVE EL Nº DE DATOS QUE HAY EN EL ARRAYLIST
        return translations.size();
    }

    public void setOnLongClickListener(View.OnLongClickListener longListener){
        this.longListener = longListener;
    }

    public void setOnClickListener(View.OnClickListener clickListener){
        this.clickListener = clickListener;
    }

    public class TranslationViewHolder extends RecyclerView.ViewHolder { // MÉTODO QUE LEE LOS DATOS DE LOS POINTS
        ListTranslationsBinding translationsBinding;

        public TranslationViewHolder(ListTranslationsBinding binding) {
            super(binding.getRoot());
            translationsBinding = binding;
        }

        public void onBindTranslations(Translation translation){
            if (translation.getNovel() != null) {
                String pg = "";
                switch (translation.getNovel()){
                    case "Classroom of the Elite":
                        pg = translation.getId().split("-")[0].replace(Constantes.ID_CLASSROOM, "");
                        translationsBinding.imgTranslation.setImageResource(R.drawable.icon_class1);
                        break;
                    case "Overlord":
                        pg = translation.getId().split("-")[0].replace(Constantes.ID_OVERLORD, "");
                        translationsBinding.imgTranslation.setImageResource(R.drawable.overlord_logo3);
                        break;
                    case "Log Horizon":
                        pg = translation.getId().split("-")[0].replace(Constantes.ID_LOGHORIZON, "");
                        translationsBinding.imgTranslation.setImageResource(R.drawable.icon_log2);
                        break;
                    case "No Game No Life":
                        pg = translation.getId().split("-")[0].replace(Constantes.ID_NGNL, "");
                        translationsBinding.imgTranslation.setImageResource(R.drawable.icon_ngnl);
                        break;
                }
                if(fragType.equals("Search") || fragType.equals("Last")){
                    String volPg = translation.getVolume() + " - " + "Pg " + pg;
                    translationsBinding.txtVolPag.setText(volPg);
                } else
                    translationsBinding.txtVolPag.setVisibility(View.INVISIBLE);
            } else {
                switch (translation.getId().split("-")[0]){
                    case Constantes.ID_CLANNAD:
                        translationsBinding.imgTranslation.setImageResource(R.drawable.icon_clannad);
                        break;
                    case Constantes.ID_GENERAL:
                        translationsBinding.imgTranslation.setImageResource(R.drawable.icon_general);
                        break;
                    case Constantes.ID_KOICHOCO:
                        translationsBinding.imgTranslation.setImageResource(R.drawable.choco);
                        break;
                }
            }

            translationsBinding.cardTranslations.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple_translations));

            translationsBinding.textWord.setText(translation.getWord());
            translationsBinding.textTranslation.setText(translation.getWordTranslation());

            translationsBinding.translationLayout.setOnClickListener(v -> {
                if(clickListener != null && (fragType.equals("Search") || fragType.equals("Last")))
                    clickListener.onClick(translationsBinding.translationLayout);
            });

            translationsBinding.translationLayout.setOnLongClickListener(v -> {
                if(longListener != null)
                    longListener.onLongClick(v);
                return false;
            });
        }
    }

    @NonNull
    @Override
    public TranslationsAdapter.TranslationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // INFLA EL LAYOUT
        return new TranslationsAdapter.TranslationViewHolder(ListTranslationsBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(TranslationsAdapter.TranslationViewHolder holder, int i) { // LEE LOS DATOS DE CADA PUNTO Y LOS MUESTRA EN EL LAYOUT
        holder.onBindTranslations(translations.get(i));
    }

    private Filter translationsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(translationsFilterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                boolean searchByType = false;

                for(Translation translation : translationsFilterList){
                    if(searchType.equals("Word"))
                        searchByType = translation.getWord().toLowerCase().contains(filterPattern);
                    else
                        searchByType = translation.getWordTranslation().toLowerCase().contains(filterPattern);

                    if(searchByType)
                        filteredList.add(translation);
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(translations != null){
                translations.clear();
                translations.addAll(filteredList);
                notifyDataSetChanged();
            }
        }
    };

    @Override
    public Filter getFilter() {
        return translationsFilter;
    }

    public void setFilterType(String searchType){
        this.searchType = searchType;
    }
}
