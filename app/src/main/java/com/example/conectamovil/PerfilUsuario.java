package com.example.conectamovil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class PerfilUsuario extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String userId;
    private ImageView perfilImageView;
    private TextView correoTextView;
    private TextView nombreTextView;
    private Button cambiarFotoButton;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userId = firebaseAuth.getCurrentUser().getUid();
        correoTextView = findViewById(R.id.correoPerfil);
        nombreTextView = findViewById(R.id.nombrePerfil);
        cambiarFotoButton = findViewById(R.id.botonFoto);
        perfilImageView = findViewById(R.id.imagenPerfil);

        obtenerDatosUsuario();

        cambiarFotoButton.setOnClickListener(view -> seleccionarNuevaFoto());
    }

    private void obtenerDatosUsuario() {
        DatabaseReference usuarioReference = databaseReference.child("Usuarios").child(userId);
        usuarioReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User usuario = snapshot.getValue(User.class);

                    if (usuario != null) {
                        correoTextView.setText(usuario.getCorreo());
                        nombreTextView.setText(usuario.getNombre());

                        if (usuario.getUrlFotoPerfil() != null && !usuario.getUrlFotoPerfil().isEmpty()) {
                            cargarImagenDesdeStorage(usuario.getUrlFotoPerfil());
                        } else {
                            perfilImageView.setImageResource(R.drawable.default_profile_image);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PerfilActivity", "Error al cargar datos del perfil: " + error.getMessage());
            }
        });
    }

    private void cargarImagenDesdeStorage(String imageUrl) {
        Picasso.get().invalidate(imageUrl);

        Picasso.get().load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(perfilImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("PerfilActivity", "Imagen cargada exitosamente desde Storage");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("PerfilActivity", "Error al cargar la imagen desde Storage: " + e.getMessage());
                        perfilImageView.setImageResource(R.drawable.default_profile_image);
                    }
                });
    }

    private void seleccionarNuevaFoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            agregarFoto(filePath);
        }
    }

    private void agregarFoto(Uri filePath) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child("perfil_imagenes/" + userId + "/imagen.jpg");

        UploadTask uploadTask = imageRef.putFile(filePath);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                DatabaseReference usuarioReference = databaseReference.child("Usuarios").child(userId);
                usuarioReference.child("urlFotoPerfil").setValue(uri.toString());

                Picasso.get().load(uri).into(perfilImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Éxito al cargar la nueva imagen
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("PerfilActivity", "Error al cargar la nueva imagen: " + e.getMessage());
                    }
                });
            }).addOnFailureListener(e -> {
                Log.e("PerfilActivity", "Error al obtener la URL de descarga: " + e.getMessage());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            Log.e("PerfilActivity", "Error al subir la imagen: " + e.getMessage());
        });
    }
}
