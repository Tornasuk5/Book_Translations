package tornasuk.translations.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tornasuk.translations.Constantes;
import tornasuk.translations.R;
import tornasuk.translations.databinding.DialogNewPagBinding;

public class NewPag extends AppCompatDialogFragment {

    private DatabaseReference firebasebdd;
    private String novel;
    private String volume;
    private String pg;

    public NewPag(String novel, String volume) {
        this.novel = novel;
        this.volume = volume;
    }

    private androidx.appcompat.app.AlertDialog newPag() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.DialogBackground);
        DialogNewPagBinding newPagBinding = DialogNewPagBinding.inflate(LayoutInflater.from(requireActivity()));
        builder.setView(newPagBinding.getRoot());

        firebasebdd = FirebaseDatabase.getInstance().getReference(novel).child(volume);

        newPagBinding.btnAddPag.setOnClickListener(v -> firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                if(!newPagBinding.editTextPag.getText().toString().trim().equals("")) {
                    boolean repeatPag = false;
                    pg = Constantes.refID + newPagBinding.editTextPag.getText().toString() + "-1";
                    for (DataSnapshot pagSnapshot : snapshot.getChildren()) {
                        if (pagSnapshot.getKey().equals(pg)) {
                            repeatPag = true;
                            Toast.makeText(getActivity(), R.string.pgExiste, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    if (!repeatPag) {
                        firebasebdd.child(pg).setValue(pg);

                        Bundle data = new Bundle();
                        data.putInt("Pg", Integer.parseInt(newPagBinding.editTextPag.getText().toString()));
                        data.putString("Novel", novel);
                        data.putString("Volume", volume);
                        Navigation.findNavController(requireActivity(), R.id.nav_host_frag).navigate(R.id.nav_translations, data);

                        dismiss();
                    }
                } else
                    Toast.makeText(getContext(), R.string.editTextBlank, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        return builder.create();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return newPag();
    }
}
