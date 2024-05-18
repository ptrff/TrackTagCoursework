package ru.ptrff.tracktag.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.ptrff.tracktag.BuildConfig;
import ru.ptrff.tracktag.R;
import ru.ptrff.tracktag.databinding.FragmentAboutAuthorBinding;
import ru.ptrff.tracktag.databinding.FragmentAboutBinding;

public class AboutAuthorFragment extends Fragment {

    private FragmentAboutAuthorBinding binding;

    public AboutAuthorFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutAuthorBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.contacts.setOnClickListener(v -> {
            showLinksDialog(new String[]{
                    "https://vk.com/i_petroff",
                    "https://t.me/i_petroff"
            });
        });
    }

    private void showLinksDialog(String[] links) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.choose_link);
        builder.setItems(links, (dialog, which) -> {
            Toast.makeText(requireContext(), " "+which, Toast.LENGTH_SHORT).show();
            followLink(links[which]);
        });
        builder.show();
    }

    private void followLink(String link){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }
}
