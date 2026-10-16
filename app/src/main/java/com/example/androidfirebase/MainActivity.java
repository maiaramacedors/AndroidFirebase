
package com.example.androidfirebase;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.text.method.ScrollingMovementMethod;

public class MainActivity extends AppCompatActivity {

    private EditText edtNomeProduto, edtPrecoProduto;
    private Button btnSalvarProduto;
    private DatabaseReference databaseReference;
    private Button btnConsultarProdutos;
    private TextView txtListaProdutos;
    private Button btnAtualizarProduto, btnExcluirProduto;
    private String idProdutoSelecionado;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtNomeProduto = findViewById(R.id.edtNomeProduto);
        edtPrecoProduto = findViewById(R.id.edtPrecoProduto);
        btnSalvarProduto = findViewById(R.id.btnSalvarProduto);
        btnConsultarProdutos = findViewById(R.id.btnConsultarProdutos);
        txtListaProdutos = findViewById(R.id.txtListaProdutos);
        txtListaProdutos.setMovementMethod(new ScrollingMovementMethod());
        btnConsultarProdutos.setOnClickListener(v -> consultarProdutos());
        btnAtualizarProduto = findViewById(R.id.btnAtualizarProduto);
        btnExcluirProduto = findViewById(R.id.btnExcluirProduto);
        btnAtualizarProduto.setOnClickListener(v -> atualizarProduto());
        btnExcluirProduto.setOnClickListener(v -> excluirProduto());


        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("produtos");

        btnSalvarProduto.setOnClickListener(v -> cadastrarProduto());
    }

    private void cadastrarProduto() {

        String nome = edtNomeProduto.getText().toString().trim();
        String precoTexto = edtPrecoProduto.getText().toString().trim();

        if (nome.isEmpty() || precoTexto.isEmpty()) {
            Toast.makeText(this,
                    "Preencha todos os campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double preco;

        try {
            preco = Double.parseDouble(
                    precoTexto.replace(",", ".")
            );
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Preço inválido",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String id = databaseReference.push().getKey();

        if (id == null) {
            Toast.makeText(this,
                    "Erro ao gerar ID",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Produto produto = new Produto(id, nome, preco);

        databaseReference.child(id)
                .setValue(produto)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this,
                            "Produto cadastrado com sucesso!",
                            Toast.LENGTH_SHORT).show();

                    edtNomeProduto.setText("");
                    edtPrecoProduto.setText("");

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Erro: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                });
    }



    private void consultarProdutos() {

        databaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        java.util.ArrayList<Produto> produtos =
                                new java.util.ArrayList<>();

                        java.util.ArrayList<String> ids =
                                new java.util.ArrayList<>();

                        for (DataSnapshot item : snapshot.getChildren()) {

                            Produto produto = item.getValue(Produto.class);

                            if (produto != null) {
                                produtos.add(produto);
                                ids.add(item.getKey());
                            }
                        }

                        if (produtos.isEmpty()) {
                            txtListaProdutos.setText(
                                    "Nenhum produto cadastrado."
                            );
                            idProdutoSelecionado = null;
                            return;
                        }

                        String[] nomes = new String[produtos.size()];

                        for (int i = 0; i < produtos.size(); i++) {
                            nomes[i] = produtos.get(i).getNome()
                                    + " - R$ "
                                    + produtos.get(i).getPreco();
                        }

                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("Selecione um produto")
                                .setItems(nomes, (dialog, posicao) -> {

                                    Produto selecionado = produtos.get(posicao);

                                    idProdutoSelecionado = ids.get(posicao);

                                    edtNomeProduto.setText(
                                            selecionado.getNome()
                                    );

                                    edtPrecoProduto.setText(
                                            String.valueOf(
                                                    selecionado.getPreco()
                                            )
                                    );

                                    txtListaProdutos.setText(
                                            "Produto selecionado:\n"
                                                    + selecionado.getNome()
                                                    + "\nPreço: R$ "
                                                    + selecionado.getPreco()
                                                    + "\nID: "
                                                    + idProdutoSelecionado
                                    );
                                })
                                .setNegativeButton("CANCELAR", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(MainActivity.this,
                                "Erro: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }



    private void atualizarProduto() {

        String nome = edtNomeProduto.getText().toString().trim();
        String precoTexto = edtPrecoProduto.getText().toString().trim();

        if (idProdutoSelecionado == null) {
            Toast.makeText(this,
                    "Selecione um produto primeiro",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (nome.isEmpty() || precoTexto.isEmpty()) {
            Toast.makeText(this,
                    "Preencha nome e preço",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double preco;

        try {
            preco = Double.parseDouble(precoTexto.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Preço inválido",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Produto produto = new Produto(
                idProdutoSelecionado, nome, preco
        );

        databaseReference.child(idProdutoSelecionado)
                .setValue(produto)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Produto atualizado!",
                            Toast.LENGTH_SHORT).show();
                    consultarProdutos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erro: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }


    private void excluirProduto() {

        if (idProdutoSelecionado == null) {
            Toast.makeText(this,
                    "Selecione um produto primeiro",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Excluir produto")
                .setMessage("Deseja realmente excluir este produto?")
                .setPositiveButton("EXCLUIR", (dialog, which) -> {

                    databaseReference.child(idProdutoSelecionado)
                            .removeValue()
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(this,
                                        "Produto excluído!",
                                        Toast.LENGTH_SHORT).show();

                                idProdutoSelecionado = null;
                                edtNomeProduto.setText("");
                                edtPrecoProduto.setText("");
                                consultarProdutos();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this,
                                        "Erro: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }

}
