package com.thaosamyn.wikimapscards.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thaosamyn.wikimapscards.LoginActivity;
import com.thaosamyn.wikimapscards.R;

public class AccountFragment extends Fragment {

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Récupération de la session Firebase active
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        TextView emailText = view.findViewById(R.id.accountEmailText);
        EditText oldPasswordInput = view.findViewById(R.id.oldPasswordInput);
        EditText newPasswordInput = view.findViewById(R.id.newPasswordInput);
        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        // Affichage de l'adresse e-mail de l'utilisateur connecté
        if (user != null) {
            emailText.setText(user.getEmail());
        }

        // Modification du mot de passe
        changePasswordButton.setOnClickListener(v -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.length() < 6) {
                Toast.makeText(getActivity(), "Remplissez les champs (6 caractères min.)", Toast.LENGTH_LONG).show();
                return;
            }

            if (user == null || user.getEmail() == null) return;

            // Firebase impose de revérifier l'ancien mot de passe avant de le changer
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    // Enregistrement du nouveau mot de passe
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(getActivity(), "Mot de passe modifié !", Toast.LENGTH_SHORT).show();
                            oldPasswordInput.setText("");
                            newPasswordInput.setText("");
                        } else {
                            Toast.makeText(getActivity(), "Erreur lors de la modification.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Mot de passe actuel incorrect.", Toast.LENGTH_LONG).show();
                }
            });
        });

        // Déconnexion et retour à l'écran de login
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            // Ferme MainActivity pour empêcher le retour vers les onglets
            requireActivity().finish();
        });

        return view;
    }
}