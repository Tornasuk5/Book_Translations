package tornasuk.translations.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tornasuk.translations.classes.Page;
import tornasuk.translations.R;
import tornasuk.translations.databinding.ListPagesBinding;

public class PagesAdapter extends RecyclerView.Adapter<PagesAdapter.PageViewHolder>{

    private final ArrayList<Page> pages;
    private final Context context;
    private View.OnClickListener clickPgListener;
    private View.OnLongClickListener longListener;

    public PagesAdapter(Context context, List<Page> pages) {
        this.pages = (ArrayList<Page>) pages;
        this.context = context;
    }

    public void setOnClickPgListener(View.OnClickListener clickPgListener){
        this.clickPgListener = clickPgListener;
    }

    public void setOnLongClickPgListener(View.OnLongClickListener longListener){
        this.longListener = longListener;
    }

    public class PageViewHolder extends RecyclerView.ViewHolder { // MÉTODO QUE LEE LOS DATOS DE LOS POINTS
        ListPagesBinding listPagesBinding;
        boolean isFirstPag;

        public PageViewHolder(ListPagesBinding binding) {
            super(binding.getRoot());
            listPagesBinding = binding;
            isFirstPag = true;
        }

        public void onBindPage(Page pag){
            if(pag.getNumPage() == 0){
                listPagesBinding.imgPgNumTranslations.setVisibility(View.INVISIBLE);
                listPagesBinding.imgAddPag.setVisibility(View.VISIBLE);
                listPagesBinding.textPg.setText("");
                listPagesBinding.textNumTraslations.setText("");
            } else {
                String pg = "Pg " + pag.getNumPage();
                listPagesBinding.textPg.setText(pg);
                listPagesBinding.textNumTraslations.setText(String.valueOf(pag.getTranslations().size()));
            }

            if(isFirstPag) {
                listPagesBinding.cardPags.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple_pags));
                isFirstPag = false;
            } else
                listPagesBinding.cardPags.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple_pags2));

            listPagesBinding.pagLayout.setOnClickListener(v -> {
                if(clickPgListener != null)
                    clickPgListener.onClick(v);
            });

            listPagesBinding.pagLayout.setOnLongClickListener(v -> {
                if(longListener != null)
                    longListener.onLongClick(v);
                return false;
            });
        }
    }

    @NonNull
    @Override
    public PagesAdapter.PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // INFLA EL LAYOUT
        return new PagesAdapter.PageViewHolder(ListPagesBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(PagesAdapter.PageViewHolder holder, int i) { // LEE LOS DATOS DE CADA PUNTO Y LOS MUESTRA EN EL LAYOUT
        holder.onBindPage(pages.get(i));
    }

    @Override
    public int getItemCount() { // DEVUELVE EL Nº DE DATOS QUE HAY EN EL ARRAYLIST
        return pages.size();
    }
}
