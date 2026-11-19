package com.thaosamyn.wikimapscards;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button registerButton = findViewById(R.id.registerButton);
        Button loginButton = findViewById(R.id.loginButton);

        // Création de compte
        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String mailAffiche = email;
                            if (user != null) {
                                mailAffiche = user.getEmail();
                            }
                            Toast.makeText(this, "Compte créé ! (" + mailAffiche + ")", Toast.LENGTH_SHORT).show();
                        } else {
                            Exception erreur = task.getException();
                            String message = "Erreur inconnue";
                            if (erreur != null) {
                                message = erreur.getMessage();
                            }
                            Toast.makeText(this, "Erreur : " + message, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Connexion
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String mailAffiche = email;
                            if (user != null) {
                                mailAffiche = user.getEmail();
                            }
                            Toast.makeText(this, "Bienvenue " + mailAffiche + " !", Toast.LENGTH_SHORT).show();
                        } else {
                            Exception erreur = task.getException();
                            if (erreur instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(this, "E-mail ou mot de passe incorrect.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Connexion impossible. Vérifiez votre réseau.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        });
    }
}