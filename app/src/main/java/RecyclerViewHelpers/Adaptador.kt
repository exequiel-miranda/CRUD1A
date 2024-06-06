package RecyclerViewHelpers

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bryan.miranda.crudbryan1a.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import modelo.ClaseConexion
import modelo.listaProductos
import java.util.UUID

class Adaptador(private var Datos: List<listaProductos>): RecyclerView.Adapter<ViewHolder>() {


    fun actualizarRecyclerView(nuevaLista: List<listaProductos>){
        Datos = nuevaLista
        notifyDataSetChanged() //Notifica que hay datos nuevos
    }

    //1- Crear la funcion de eliminar
    fun eliminarRegistro(nombreProducto: String, posicion: Int){
        //Notificar al adaptador
        val listaDatos = Datos.toMutableList()
        listaDatos.removeAt(posicion)

        //Quitar de la base de datos
        GlobalScope.launch(Dispatchers.IO){
            //Dos pasos para eliminar de la base de datos

            //1- Crear un objeto de la clase conexion
            val objConexion = ClaseConexion().cadenaConexion()

            //2- Creo una variable que contenga un PrepareStatement
            val deleteProducto = objConexion?.prepareStatement("delete tbProductos1 where nombreProducto = ?")!!
            deleteProducto.setString(1, nombreProducto)
            deleteProducto.executeUpdate()

            val commit = objConexion.prepareStatement("commit")
            commit.executeUpdate()
        }

        //Notificamos el cambio para que refresque la lista
        Datos = listaDatos.toList()
        //Quito los datos de la lista
        notifyItemRemoved(posicion)
        notifyDataSetChanged()
    }


    fun actualizarListadoDespuesDeEditar(uuid: String, nuevoNombre: String){
        //Obtener el UUID
        val identificador = Datos.indexOfFirst { it.uuid == uuid }
        //Asigno el nuevo nombre
        Datos[identificador].nombreProducto = nuevoNombre
        //Notifico que los cambios han sido realizados
        notifyItemChanged(identificador)
    }


    //Creamos la funcion de editar o actualizar en la base de datos
    fun editarProducto(nombreProducto: String,uuid: String){
        //-Creo una corrutina
        GlobalScope.launch(Dispatchers.IO){
            //1- Creo un objeto de la clase conexion
            val objConexion = ClaseConexion().cadenaConexion()

            //2- Creo una variable que contenga un PrepareStatement
            val updateProducto = objConexion?.prepareStatement("update tbProductos1 set nombreProducto = ? where uuid = ?")!!
            updateProducto.setString(1, nombreProducto)
            updateProducto.setString(2, uuid)
            updateProducto.executeUpdate()

            val commit = objConexion.prepareStatement("commit")
            commit.executeUpdate()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.activity_itam_card, parent, false)
        return ViewHolder(vista)
    }

    override fun getItemCount() = Datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = Datos[position]
        holder.textView.text = producto.nombreProducto

        //Darle clic al icono de borrar
        holder.imgBorrar.setOnClickListener {

            //Crear una alerta de confirmacion para que se borre
            val context = holder.textView.context

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Eliminar")
            builder.setMessage("¿Estas seguro que deseas eliminar?")

            //botones de mi alerta
            builder.setPositiveButton("Si"){
                dialog, wich ->
                eliminarRegistro(producto.nombreProducto, position)
            }

            builder.setNegativeButton("No"){
                dialog, wich ->
                //Si doy en clic en "No" se cierra la alerta
                dialog.dismiss()
            }

            //Para mostrar la alerta
            val dialog = builder.create()
            dialog.show()
        }

    }


}