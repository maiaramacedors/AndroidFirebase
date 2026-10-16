
package com.example.androidfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtSenha;

    private Button btnLogin;
    private Button btnCadastrar;
    private Button btnGoogle;

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        btnLogin = findViewById(R.id.btnLogin);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnGoogle = findViewById(R.id.btnGoogle);

        // Cadastro com e-mail e senha
        btnCadastrar.setOnClickListener(v -> cadastrar());

        // Login com e-mail e senha
        btnLogin.setOnClickListener(v -> login());

        // Configuração do login com Google
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(
                        GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(
                                getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent =
                    googleSignInClient.getSignInIntent();

            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    // Recebe o resultado do login com Google
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
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
                Toast.makeText(this,
                        "Erro no login Google: " + e.getStatusCode(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Autentica a conta Google no Firebase
    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Login com Google realizado!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(
                                new Intent(this, HomeActivity.class));

                        finish();

                    } else {

                        Toast.makeText(this,
                                "Falha na autenticação com Google",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Cadastro com e-mail e senha
    private void cadastrar() {

        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();

        if (email.isEmpty() || senha.isEmpty()) {

            Toast.makeText(this,
                    "Preencha email e senha",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Conta criada com sucesso!",
                                Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(this,
                                "Erro: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Login com e-mail e senha
    private void login() {

        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();

        if (email.isEmpty() || senha.isEmpty()) {

            Toast.makeText(this,
                    "Preencha email e senha",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Login realizado!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(
                                new Intent(this, HomeActivity.class));

                        finish();

                    } else {

                        Toast.makeText(this,
                                "Erro no login",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
