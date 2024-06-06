package bryan.miranda.crudbryan1a

import RecyclerViewHelpers.Adaptador
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.ClaseConexion
import modelo.listaProductos
import java.util.UUID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //1- Mandar a llamar todos los elementos de la vista
        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val txtPrecio = findViewById<EditText>(R.id.txtPrecio)
        val txtCantidad = findViewById<EditText>(R.id.txtCantidad)
        val btnAgregar = findViewById<Button>(R.id.btnAgregar)
        val rcvDatos = findViewById<RecyclerView>(R.id.rcvDatos)

        rcvDatos.layoutManager = LinearLayoutManager(this)

        ////////////Funcion para mostrar datos
        fun obtenerDatos(): List<listaProductos> {
            //1- Creo una objeto de la clase conexion
            val objConexion = ClaseConexion().cadenaConexion()

            val statement = objConexion?.createStatement()
            val resultSet = statement?.executeQuery("select * from tbProductos1")!!

            val listadoProductos = mutableListOf<listaProductos>()

            //Recorrer todos los datos que me trajo el select
            while (resultSet.next()){
                val nombre = resultSet.getString("nombreProducto")
                val producto = listaProductos(nombre)
                listadoProductos.add(producto)
            }
            return listadoProductos
        }

        //Ejecutamos la funcion
        CoroutineScope(Dispatchers.IO).launch {
            val ejecutarFuncion = obtenerDatos()


            withContext(Dispatchers.Main){
                //Asigno el adaptador mi RecyclerView
                //(Uno mi Adaptador con el RecyclerView)
                val miAdaptador = Adaptador(ejecutarFuncion)
                rcvDatos.adapter = miAdaptador
            }
        }


        //2- Programar el boton de agregar
        btnAgregar.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO){

                //1- Crear un objeto de la clase de conexion
                val objConexion = ClaseConexion().cadenaConexion()

                //2- Crear una variable que sea igual a un PrepareStatement
                val addProducto = objConexion?.prepareStatement("insert into tbProductos1(uuid, nombreProducto, precio, cantidad) values(?, ?, ?, ?)")!!
                addProducto.setString(1, UUID.randomUUID().toString())
                addProducto.setString(2, txtNombre.text.toString())
                addProducto.setInt(3, txtPrecio.text.toString().toInt())
                addProducto.setInt(4, txtCantidad.text.toString().toInt())

                addProducto.executeUpdate()

                val nuevosProductos = obtenerDatos()

                //Creo una corrutina que actualice el listado
                withContext(Dispatchers.Main){
                    (rcvDatos.adapter as? Adaptador)?.actualizarRecyclerView(nuevosProductos)
                }

            }
        }

    }
}

