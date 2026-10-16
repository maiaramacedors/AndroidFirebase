
package com.example.androidfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private TextView txtUsuario;
    private Button btnLogout, btnProdutos;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtUsuario = findViewById(R.id.txtUsuario);
        btnLogout = findViewById(R.id.btnLogout);
        btnProdutos = findViewById(R.id.btnProdutos);

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            txtUsuario.setText("Logado: " + user.getEmail());
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Configuração do Google Sign-In
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(
                                getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Abrir gerenciamento de produtos
        btnProdutos.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        // Sair da conta
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {

        // Encerra a sessão do Firebase
        FirebaseAuth.getInstance().signOut();

        // Encerra a sessão do Google
        googleSignInClient.signOut().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Toast.makeText(this,
                        "Logout realizado com sucesso!",
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this,
                        "Sessão do Firebase encerrada",
                        Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(
                    this, LoginActivity.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }
}
