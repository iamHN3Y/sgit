package dao;

import models.Producto;
import models.Stock;
import models.Usuario;
import utilities.database.Conexion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

interface I_DAO_Producto {

    boolean createProducto(Producto p, Usuario u, Stock s) throws SQLException;

    Producto readProducto(int id);

    DefaultComboBoxModel<Producto> listaProductos() throws SQLException;

    DefaultTableModel tablaProductos() throws SQLException;

    boolean updateProducto(Producto p, Usuario u, Stock s) throws SQLException;

    boolean deleteProducto(int id, Usuario u) throws SQLException;

    DefaultComboBoxModel<Producto> listaProductosProveedor(int id) throws SQLException;

}

public class DAO_Producto extends Conexion implements I_DAO_Producto {

    /**
     * Crea un nuevo producto en la base de datos.
     *
     * @param p El objeto Producto que se va a crear.
     * @param u El Usuario que realiza la operación.
     * @param s El Stock asociado al producto.
     * @return OperacionResultado que indica el resultado de la operación
     * (EXITO, ERROR_BD o OTRO_ERROR).
     */
    @Override
    public boolean createProducto(Producto p, Usuario u, Stock s) {
        boolean resultado = false;
        try {
            resultado = create(p, u, s);
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(null, "Checale we pq hubo un error-> " + e);
        }
        return resultado;
    }

    /**
     * Método privado que realiza la creación real del Producto en la base de
     * datos.
     *
     * @param p El objeto Producto que se va a crear.
     * @param u El Usuario que realiza la operación.
     * @param s El Stock asociado al producto.
     * @return OperacionResultado que indica el resultado de la operación
     * (EXITO, ERROR_BD o OTRO_ERROR).
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private boolean create(Producto p, Usuario u, Stock s) throws SQLException, Exception {
        try {
            this.conectar();
            String query = "insert into producto(nombre, descripcion, precio, borrado, fecha_modificacion, id_proveedor, preciocom) values (?, ?, ?, ?, NOW(),?,?);";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setFloat(3, p.getPrecio());
            ps.setBoolean(4, false);
            ps.setInt(5, p.getId_proveedor());
            ps.setFloat(6, p.getPreciocom());

            boolean resul = ps.execute();
            s.setId_producto(searchID(p));
            if (resul != true) {
                if (new DAO_Stock().insertStock(s)) {
                    new DAO_control_log().insertControl("Creo un producto", u);
                    return true;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            this.cerrar();
        }
        return false;
    }

    /**
     * Busca y devuelve el ID de un Producto en la base de datos.
     *
     * @param p El objeto Producto para el cual se busca el ID.
     * @return El ID del Producto encontrado o 0 si no se encuentra.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int searchID(Producto p) throws SQLException {
        int id = 0;
        try {
            this.conectar();
            String query = "select id from producto where nombre = ? and descripcion = ? and precio = ?;";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setFloat(3, p.getPrecio());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
                return id;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.cerrar();
        }
        return id;
    }

    /**
     * Lee y devuelve una lista de productos desde la base de datos a través de
     * una vista.
     *
     * @return ArrayList de Productos que contiene los datos recuperados de la
     * vista.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public ArrayList<Producto> readProductos() throws SQLException {
        ArrayList<Producto> productos = new ArrayList<>();
        try {
            this.conectar();
            String query = "select * from vista_listproductos;";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Producto p = new Producto();
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getFloat("precio"));
                p.setStock(rs.getInt("cantidad"));
                p.setNombre_proveedor(rs.getString("proveedor"));
                p.setPreciocom(rs.getFloat("precio_compra"));
                p.setId_proveedor(rs.getInt("id_proveedor"));
                productos.add(p);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.cerrar();
        }
        return productos;
    }

    /**
     * Lee y devuelve un producto específico de la base de datos según su ID.
     *
     * @param id El ID del producto que se desea leer.
     * @return El Producto recuperado de la base de datos, o un objeto Producto
     * vacío si no se encuentra.
     * @throws RuntimeException Sí ocurre un error durante la operación.
     */
    @Override
    public Producto readProducto(int id) {
        Producto p = new Producto();
        try {
            this.conectar();
            String query = "select * from vista_listproductos where id = ?;";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                p.setId(rs.getInt("id"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getFloat("precio"));
                p.setStock(rs.getInt("cantidad"));
                p.setNombre_proveedor(rs.getString("proveedor"));
                p.setPreciocom(rs.getFloat("precio_compra"));
                p.setId_proveedor(rs.getInt("id_proveedor"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    /**
     * Genera un modelo de ComboBox por defecto (DefaultComboBoxModel) que
     * contiene una lista de productos obtenidos de la base de datos.
     *
     * @return DefaultComboBoxModel que contiene la lista de productos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    @Override
    public DefaultComboBoxModel<Producto> listaProductos() throws SQLException {
        DefaultComboBoxModel<Producto> modelo = new DefaultComboBoxModel<>();
        for (Producto p : readProductos()) {
            modelo.addElement(p);
        }
        return modelo;
    }

    /**
     * Genera un modelo de tabla por defecto (DefaultTableModel) que representa
     * una tabla de productos con columnas para ID, Nombre, Descripción, Precio
     * y Cantidad. Los datos se obtienen de la base de datos.
     *
     * @return DefaultTableModel que representa la tabla de productos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    @Override
    public DefaultTableModel tablaProductos() throws SQLException {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("ID");
        model.addColumn("Nombre");
        model.addColumn("Descripción");
        model.addColumn("Precio Venta");
        model.addColumn("Cantidad");
        model.addColumn("Proveedor");
        model.addColumn("Precio Compra");
        model.addColumn("ID Proveedor");
        for (Producto p : readProductos()) {
            Object[] fila = new Object[8];
            fila[0] = p.getId();
            fila[1] = p.getNombre();
            fila[2] = p.getDescripcion();
            fila[3] = p.getPrecio();
            fila[4] = p.getStock();
            fila[5] = p.getNombre_proveedor();
            fila[6] = p.getPreciocom();
            fila[7] = p.getId_proveedor();
            model.addRow(fila);
        }
        return model;
    }

    /**
     * Actualiza un producto en la base de datos con los nuevos datos
     * proporcionados.
     *
     * @param p El objeto Producto con los nuevos datos.
     * @param u El Usuario que realiza la operación.
     * @param s El Stock asociado al producto.
     * @return OperacionResultado que indica el resultado de la operación
     * (EXITO, ERROR_BD o OTRO_ERROR).
     */
    @Override
    public boolean updateProducto(Producto p, Usuario u, Stock s) {
        boolean resultado = false;
        try {
            resultado = update(p, u, s);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Checale we pq hubo un error-> " + e);
        }
        return resultado;
    }

    /**
     * Método privado que realiza la actualización real del Producto en la base
     * de datos.
     *
     * @param p El objeto Producto con los nuevos datos.
     * @param u El Usuario que realiza la operación.
     * @param s El Stock asociado al producto.
     * @return OperacionResultado que indica el resultado de la operación
     * (EXITO, ERROR_BD o OTRO_ERROR).
     */
    private boolean update(Producto p, Usuario u, Stock s) throws Exception {
        try {
            this.conectar();
            String query = "update producto set nombre = ?, descripcion = ?, precio = ?, fecha_modificacion = NOW(), preciocom = ?  where id = ?;";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setFloat(3, p.getPrecio());
            ps.setFloat(4, p.getPreciocom());
            ps.setInt(5, p.getId());

            boolean resul = ps.execute();
            s.setId_producto(p.getId());
            if (resul != true) {
                if (new DAO_Stock().updateStock(s)) {
                    new DAO_control_log().insertControl("Actualizo un producto", u);
                    return true;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    /**
     * Elimina un producto de la base de datos marcándolo como "borrado".
     *
     * @param id El ID del producto que se va a eliminar.
     * @param u El Usuario que realiza la operación.
     * @return OperacionResultado que indica el resultado de la operación
     * (EXITO, ERROR_BD o OTRO_ERROR).
     */
    @Override
    public boolean deleteProducto(int id, Usuario u) {
        boolean resultado = false;
        try {
            resultado = delete(id, u);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Checale we pq hubo un error-> " + e);
        }
        return resultado;
    }

    /**
     * Método privado que realiza la eliminación real del Producto en la base de
     * datos, marcándolo como "borrado".
     *
     * @param id El ID del producto que se va a eliminar.
     * @param u El Usuario que realiza la operación.
     * @return OperacionResultado que indica el resultado de la operación
     * (EXITO, ERROR_BD o OTRO_ERROR).
     */
    private boolean delete(int id, Usuario u) throws Exception {
        try {
            this.conectar();
            String query = "update producto set borrado = ? where id = ?";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ps.setBoolean(1, true);
            ps.setInt(2, id);
            boolean resul = ps.execute();
            if (resul != true) {
                new DAO_control_log().insertControl("Elimino un producto", u);
                return true;
            }
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    public ArrayList<Producto> filtrarProductosPorProveedor(int id) throws Exception {
        ArrayList<Producto> productosFiltrados = new ArrayList<>();

        try {
            this.conectar();
            String query = "SELECT * FROM vista_listproductos WHERE id_proveedor = ?;";
            try (PreparedStatement ps = this.conexion.prepareStatement(query)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Producto p = new Producto();
                        p.setId(rs.getInt("id"));
                        p.setNombre(rs.getString("nombre"));
                        p.setDescripcion(rs.getString("descripcion"));
                        p.setPrecio(rs.getFloat("precio"));
                        p.setStock(rs.getInt("cantidad"));
                        p.setNombre_proveedor(rs.getString("proveedor"));
                        p.setPreciocom(rs.getFloat("precio_compra"));
                        p.setId_proveedor(rs.getInt("id_proveedor"));
                        productosFiltrados.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al filtrar productos por proveedor", e);
        } finally {
            this.cerrar();
        }

        return productosFiltrados;
    }

    @Override
    public DefaultComboBoxModel<Producto> listaProductosProveedor(int id) throws SQLException {
        DefaultComboBoxModel<Producto> modelo = new DefaultComboBoxModel<>();
        try {
            for (Producto p : filtrarProductosPorProveedor(id)) {
                modelo.addElement(p);
            }
        } catch (Exception ex) {
            Logger.getLogger(DAO_Producto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return modelo;
    }

    public ArrayList<Object[]> vista_producto_critico() {
        ArrayList<Object[]> listaProductosCriticos = new ArrayList<>();
        try {
            this.conectar();
            String query = "select * from vista_producto_critico;";
            try (PreparedStatement ps = this.conexion.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] obj = new Object[2];
                        obj[0] = rs.getString("nombre_producto");
                        obj[1] = rs.getString("stock_actual");
                        listaProductosCriticos.add(obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaProductosCriticos;
    }

    public ArrayList<Object[]> vista_producto_mas_vendido() {
        ArrayList<Object[]> listaProductosMasVendidos = new ArrayList<>();
        try {
            this.conectar();
            String query = "SELECT * FROM sgit.vista_producto_mas_vendido;";
            try (PreparedStatement ps = this.conexion.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] obj = new Object[2];
                        obj[0] = rs.getString("nombre_producto");
                        obj[1] = rs.getString("total_vendido");
                        listaProductosMasVendidos.add(obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaProductosMasVendidos;
    }

    public ArrayList<Object[]> vista_producto_menos_vendido() {
        ArrayList<Object[]> listaProductosMenosVendidos = new ArrayList<>();
        try {
            this.conectar();
            String query = "SELECT * FROM sgit.vista_producto_menos_vendido;";
            try (PreparedStatement ps = this.conexion.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] obj = new Object[2];
                        obj[0] = rs.getString("nombre_producto");
                        obj[1] = rs.getString("total_vendido");
                        listaProductosMenosVendidos.add(obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaProductosMenosVendidos;
    }
    
    public ArrayList<Object[]> vista_producto_recomendable() {
        ArrayList<Object[]> listaProductosRecomendable = new ArrayList<>();
        try {
            this.conectar();
            String query = "SELECT * FROM sgit.vista_producto_recomendable;";
            try (PreparedStatement ps = this.conexion.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] obj = new Object[2];
                        obj[0] = rs.getString("nombre_producto");
                        obj[1] = rs.getString("stock_actual");
                        listaProductosRecomendable.add(obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaProductosRecomendable;
    }

}
