package ru.ptrff.tracktag.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.ptrff.tracktag.R;
import ru.ptrff.tracktag.api.FirebaseHelper;
import ru.ptrff.tracktag.data.UserData;
import ru.ptrff.tracktag.databinding.ActivityAuthBinding;
import ru.ptrff.tracktag.viewmodels.AuthViewModel;
import ru.ptrff.tracktag.views.MainActivity;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private AuthViewModel viewModel;
    private AlertDialog loading;

    private boolean loginAction = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserData userData = UserData.getInstance();
        userData.restoreData(getApplicationContext().getSharedPreferences("UserData", MODE_PRIVATE));

        if (userData.isLoggedIn()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        FirebaseHelper.getInstance().init();

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initClickListeners();
        initObservers();
        initLoading();
    }

    private void initLoading() {
        loading = new MaterialAlertDialogBuilder(this)
                .setView(R.layout.dialog_loading)
                .setCancelable(false)
                .create();
    }


    private void initClickListeners() {
        binding.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.password.clearFocus();
            }
            return false;
        });

        binding.toggleButton.addOnButtonCheckedListener((materialButtonToggleGroup, checkedId, checked) -> {
            if (materialButtonToggleGroup.getCheckedButtonId() == R.id.sign_in) {
                binding.usernameLayout.setVisibility(View.GONE);
                binding.goButton.setText(getResources().getString(R.string.login));
                loginAction = true;
            } else {
                binding.usernameLayout.setVisibility(View.VISIBLE);
                binding.goButton.setText(getResources().getString(R.string.register));
                loginAction = false;
            }
        });

        binding.goButton.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String login = binding.username.getText().toString();
            String password = binding.password.getText().toString();

            if (loginAction) {
                if (badFields(email, password)) return;

                viewModel.login(email, password);
                loading.show();
            } else {
                if (badFields(email, login, password)) return;

                viewModel.register(email, login, password);
                loading.show();
            }
        });
    }

    private void initObservers() {
        viewModel.getAuthError().observe(
                this,
                isError -> {
                    if (isError) {
                        Toast.makeText(this, viewModel.getAuthErrorText(), Toast.LENGTH_SHORT).show();
                    }
                    loading.dismiss();
                }
        );

        viewModel.getLoggedIn().observe(this, loggedIn -> {
            if (loggedIn) {
                Toast.makeText(this, R.string.you_have_logged_in, Toast.LENGTH_SHORT).show();
                loading.dismiss();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean badFields(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.empty_fields, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (email.length() < 4) {
            Toast.makeText(this, R.string.email_too_short, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (password.length() <= 6) {
            Toast.makeText(this, R.string.password_too_short, Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private boolean badFields(String email, String login, String password) {
        if (email.isEmpty() || login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.empty_fields, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (email.length() < 4) {
            Toast.makeText(this, R.string.email_too_short, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (password.length() < 6) {
            Toast.makeText(this, R.string.password_too_short, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (login.length() < 3) {
            Toast.makeText(this, R.string.username_too_short, Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }
}