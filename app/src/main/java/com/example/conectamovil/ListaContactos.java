package com.example.conectamovil;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class ListaContactos extends AppCompatActivity {

    private EditText editTextNombreContacto, editTextTelefonoContacto;
    private Button btnAgregarContacto;
    private ListView listViewContactos;
    private ArrayList<Contacto> listaContactos;
    private ArrayAdapter<Contacto> adapter;
    private SearchView searchViewContacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        editTextNombreContacto = findViewById(R.id.editTextNombreContacto);
        editTextTelefonoContacto = findViewById(R.id.editTextTelefonoContacto);
        btnAgregarContacto = findViewById(R.id.btnAgregarContacto);
        listViewContactos = findViewById(R.id.listViewContactos);
        searchViewContacto = findViewById(R.id.searchViewContactos);

        listaContactos = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaContactos);
        listViewContactos.setAdapter(adapter);

        btnAgregarContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarContacto();
            }
        });

        cargarContactos();

        searchViewContacto.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Restablecer la lista original cuando el SearchView pierde el foco
                if (!hasFocus) {
                    adapter.clear();
                    adapter.addAll(listaContactos);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        searchViewContacto.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Puedes realizar alguna acción si se envía la consulta de búsqueda
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtrar la lista al cambiar el texto de búsqueda
                filtrarContactos(newText);
                return true;
            }
        });
    }

    private void agregarContacto() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String nombre = editTextNombreContacto.getText().toString();
        String telefono = editTextTelefonoContacto.getText().toString();

        if (!nombre.isEmpty() && !telefono.isEmpty()) {
            Contacto nuevoContacto = new Contacto(nombre, telefono);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contactos").child(uid);
            databaseReference.push().setValue(nuevoContacto);

            editTextNombreContacto.setText("");
            editTextTelefonoContacto.setText("");
        }
    }

    private void cargarContactos() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contactos").child(uid);

        Query query = databaseReference.orderByChild("nombre");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaContactos.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    String telefono = snapshot.child("telefono").getValue(String.class);

                    Contacto contacto = new Contacto(nombre, telefono);
                    listaContactos.add(contacto);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejar errores si es necesario
            }
        });
    }

    private void filtrarContactos(String query) {
        // Crear una lista temporal para almacenar los resultados de la búsqueda
        ArrayList<Contacto> listaFiltrada = new ArrayList<>();

        // Verificar si la consulta está vacía o contiene solo espacios en blanco
        if (query.trim().isEmpty()) {
            // Si la consulta está vacía, mostrar todos los contactos
            listaFiltrada.addAll(listaContactos);
        } else {
            // Convertir la consulta y los nombres de contactos a minúsculas
            String queryLowerCase = query.toLowerCase();

            // Iterar a través de la lista de contactos y agregar aquellos que coincidan con la búsqueda
            for (Contacto contacto : listaContactos) {
                // Convertir el nombre del contacto a minúsculas
                String nombreLowerCase = contacto.getNombre().toLowerCase();

                if (nombreLowerCase.contains(queryLowerCase) || contacto.getTelefono().contains(query)) {
                    listaFiltrada.add(contacto);
                }
            }
        }

        // Actualizar el adaptador con la lista filtrada
        adapter.clear();
        adapter.addAll(listaFiltrada);
        adapter.notifyDataSetChanged();
    }

}
