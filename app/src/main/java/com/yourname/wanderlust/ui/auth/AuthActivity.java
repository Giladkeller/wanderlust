package com.yourname.wanderlust.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.yourname.wanderlust.MainActivity;
import com.yourname.wanderlust.R;
import com.yourname.wanderlust.databinding.ActivityAuthBinding;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        // אם כבר מחובר — עבור ישר
        if (auth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        setupGoogleSignIn();
        setupButtons();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("697611578561-cqrk0s0qp8bh9gpkim2h7aufvhsopb3o.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupButtons() {

        // Google Sign In
        binding.btnGoogle.setOnClickListener(v ->
                startActivityForResult(
                        googleClient.getSignInIntent(), RC_SIGN_IN));

        // Email Register
        binding.btnRegister.setOnClickListener(v -> {
            String email    = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString().trim();
            String username = binding.editUsername.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "מלא את כל השדות",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        // שמור שם משתמש
                        result.getUser().updateProfile(
                                new com.google.firebase.auth.UserProfileChangeRequest
                                        .Builder()
                                        .setDisplayName(username)
                                        .build());
                        goToMain();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "שגיאה: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });

        // Email Login
        binding.btnLogin.setOnClickListener(v -> {
            String email    = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "מלא אימייל וסיסמה",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(r -> goToMain())
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "שגיאה: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account =
                        task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign In נכשל",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);
        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(r -> goToMain())
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "שגיאה: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean loading) {
        binding.progressAuth.setVisibility(
                loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnRegister.setEnabled(!loading);
        binding.btnGoogle.setEnabled(!loading);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}