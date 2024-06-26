package ru.ptrff.tracktag.views;

import static ru.ptrff.tracktag.data.OptionActions.SAVE;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.ptrff.tracktag.R;
import ru.ptrff.tracktag.adapters.OptionsListAdapter;
import ru.ptrff.tracktag.api.FirebaseHelper;
import ru.ptrff.tracktag.data.OptionActions;
import ru.ptrff.tracktag.data.Options;
import ru.ptrff.tracktag.data.UserData;
import ru.ptrff.tracktag.databinding.FragmentMoreBinding;
import ru.ptrff.tracktag.interfaces.MainFragmentCallback;
import ru.ptrff.tracktag.interfaces.MoreFragmentCallback;

public class MoreFragment extends Fragment {

    private FragmentMoreBinding binding;
    private MoreFragmentCallback callback;
    private boolean isAuthorized = false;

    public MoreFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callback = (MoreFragmentCallback) requireActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        binding.optionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        OptionsListAdapter adapter = new OptionsListAdapter(
                getLayoutInflater(),
                Options.more
        );
        adapter.setOnOptionClickListener(option -> callback.performAction(option.getAction()));
        binding.optionsList.setAdapter(adapter);

        binding.logout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setCancelable(true)
                    .setPositiveButton(R.string.logout, (dialog, which) -> {
                        dialog.dismiss();
                        UserData.getInstance().logout();
                        Toast.makeText(requireContext(),  R.string.you_have_logged_out, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireContext(), AuthActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton(R.string.stay, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setTitle(R.string.are_you_sure_logout)
                    .setMessage(R.string.all_data_will_be_deleted)
                    .create().show();
        });

        if (UserData.getInstance().isLoggedIn()) {
            final TypedValue value = new TypedValue();
            requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorAccent, value, true);
            ColorStateList colorStateList = ColorStateList.valueOf(value.data);
            binding.profilePic.setImageTintList(colorStateList);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UserData data = UserData.getInstance();
        if (data.isLoggedIn()) {
            String username = UserData.getInstance().getUserName();
            binding.greetings.setText(getResources().getString(R.string.greetings, username));

            binding.notLoggedIn.setVisibility(View.GONE);
            binding.logout.setVisibility(View.VISIBLE);
        } else {
            binding.greetings.setText(getResources().getString(R.string.greetings_new));
        }
    }
}