package org.store.narzedziuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.store.narzedziuz.R;
import org.store.narzedziuz.models.AppUser;
import org.store.narzedziuz.repositories.UserRepository;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$");

    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    // CAPTCHA
    private View captchaWidget;
    private CheckBox captchaCheckbox;
    private ProgressBar captchaProgress;
    private boolean captchaVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        etFirstName       = findViewById(R.id.et_first_name);
        etLastName        = findViewById(R.id.et_last_name);
        etEmail           = findViewById(R.id.et_email);
        etPassword        = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister       = findViewById(R.id.btn_register);
        tvLogin           = findViewById(R.id.tv_login);
        progressBar       = findViewById(R.id.progress_bar);

        captchaWidget   = findViewById(R.id.captcha_widget);
        captchaCheckbox = captchaWidget.findViewById(R.id.captcha_checkbox);
        captchaProgress = captchaWidget.findViewById(R.id.captcha_progress);

        captchaWidget.setOnClickListener(v -> handleCaptchaClick());

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    // ---------------------------------------------------------------
    // CAPTCHA
    // ---------------------------------------------------------------

    private void handleCaptchaClick() {
        if (captchaVerified) return;

        // Pokaż spinner przez 1.2 s, potem zatwierdź
        captchaProgress.setVisibility(View.VISIBLE);
        captchaWidget.setClickable(false);

        new Handler().postDelayed(() -> {
            captchaProgress.setVisibility(View.GONE);
            captchaCheckbox.setChecked(true);
            captchaVerified = true;
        }, 1200);
    }

    // ---------------------------------------------------------------
    // Rejestracja
    // ---------------------------------------------------------------

    private void attemptRegister() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();
        String confirm   = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) { etFirstName.setError(getString(R.string.register_error_empty_first_name)); return; }
        if (TextUtils.isEmpty(lastName))  { etLastName.setError(getString(R.string.register_error_empty_last_name)); return; }
        if (!EMAIL_PATTERN.matcher(email).matches()) { etEmail.setError(getString(R.string.register_error_invalid_email)); return; }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            etPassword.setError(getString(R.string.register_error_invalid_password));
            return;
        }
        if (!password.equals(confirm)) { etConfirmPassword.setError(getString(R.string.register_error_passwords_mismatch)); return; }

        if (!captchaVerified) {
            Toast.makeText(this, R.string.register_error_captcha_required, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    AppUser user = new AppUser(uid, email, firstName, lastName);
                    UserRepository.getInstance().saveUser(user, new org.store.narzedziuz.callbacks.OnComplete() {
                        @Override public void onSuccess() {
                            setLoading(false);
                            Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        }
                        @Override public void onFailure(Exception e) {
                            setLoading(false);
                            Toast.makeText(RegisterActivity.this, getString(R.string.register_error_profile_save, e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.register_error_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }
}