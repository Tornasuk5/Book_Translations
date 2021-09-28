package tornasuk.translations.adapters;

import android.content.Context;
import android.view.LayoutInflater;

import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tornasuk.translations.classes.Novel;
import tornasuk.translations.R;
import tornasuk.translations.databinding.ListHomeBinding;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder>{

    private final ArrayList<Novel> novels;
    private final Context context;

    public HomeAdapter(Context context, List<Novel> novels) {
        this.context = context;
        this.novels = (ArrayList<Novel>) novels;
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder { // MÉTODO QUE LEE LOS DATOS DE LOS POINTS
        ListHomeBinding listHomeBinding;
        ProgressBarAnimation pba;

        public HomeViewHolder(ListHomeBinding binding) {
            super(binding.getRoot());
            listHomeBinding = binding;
        }

        public void onBindLibros(Novel novel){
            switch (novel.getNovelName()) {
                case "General":
                    listHomeBinding.imgLibroHome.setImageResource(R.drawable.icon_general);
                    break;
                case "Clannad ~After Story~":
                    listHomeBinding.imgLibroHome.setImageResource(R.drawable.icon_clannad);
                    break;
                case "Overlord":
                    listHomeBinding.imgLibroHome.setImageResource(R.drawable.overlord_logo3);
                    break;
                case "Log Horizon":
                    listHomeBinding.imgLibroHome.setImageResource(R.drawable.icon_log2);
                    break;
                case "Classroom of the Elite":
                    listHomeBinding.imgLibroHome.setImageResource(R.drawable.icon_class1);
                    break;
                case "No Game No Life":
                    listHomeBinding.imgLibroHome.setImageResource(R.drawable.icon_ngnl);
                    break;
            }

            listHomeBinding.txtLibroHome.setText(novel.getNovelName());
            try {
                String numVolumes = novel.getNumStartedVolumes() + "/" + novel.getVolumes().size();
                listHomeBinding.txtNumVolumesHome.setText(numVolumes);
            } catch (NullPointerException npx){
                listHomeBinding.txtNumVolumesHome.setText("-");
            }

            String txtPto = novel.getTranslationsPercent() + "%";
            listHomeBinding.ptoHome.setText(txtPto);

            pba = new ProgressBarAnimation(listHomeBinding, 0, (float) novel.getTranslationsPercent());
            pba.setDuration(1000);
            listHomeBinding.ptoHome.startAnimation(pba);
            listHomeBinding.progBarHome.startAnimation(pba);
        }
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeViewHolder(ListHomeBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(HomeAdapter.HomeViewHolder holder, int i) { // LEE LOS DATOS DE CADA PUNTO Y LOS MUESTRA EN EL LAYOUT
        holder.onBindLibros(novels.get(i));
    }

    @Override
    public int getItemCount() { // DEVUELVE EL Nº DE DATOS QUE HAY EN EL ARRAYLIST
        return novels.size();
    }

    public class ProgressBarAnimation extends Animation {
        ListHomeBinding listHomeBinding;
        float from;
        float  to;

        public ProgressBarAnimation(ListHomeBinding listHomeBinding, float from, float to) {
            super();
            this.listHomeBinding = listHomeBinding;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            int pto = (int) value;
            listHomeBinding.progBarHome.setProgress(pto);
            listHomeBinding.ptoHome.setText(String.valueOf(pto+"%"));
        }
    }
}
