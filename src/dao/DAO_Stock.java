package dao;

import models.Stock;
import utilities.database.Conexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

interface I_DAO_Stock {

    boolean insertStock(Stock s) throws SQLException;

    boolean updateStock(Stock s) throws SQLException;

    Stock searchStock(int id) throws SQLException;
}

public class DAO_Stock extends Conexion implements I_DAO_Stock {

    /**
     * Inserta un nuevo registro de Stock en la base de datos para un producto
     * específico.
     *
     * @param s El objeto Stock que contiene los datos del nuevo registro.
     * @return true si la inserción es exitosa, false en caso contrario.
     */
    @Override
    public boolean insertStock(Stock s) {
        try {
            this.conectar();
            String query = "insert into stock(id_producto, cantidad) values (?,?);";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ps.setInt(1, s.getId_producto());
            ps.setInt(2, s.getCantidad());
            boolean resul = ps.execute();
            if (resul != true) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Actualiza la cantidad de Stock en la base de datos para un producto
     * específico.
     *
     * @param s El objeto Stock con la nueva cantidad a actualizar.
     * @return true si la actualización es exitosa, false en caso contrario.
     */
    @Override
    public boolean updateStock(Stock s) {
        try {
            this.conectar();
            String query = "update stock set cantidad = ? where id_producto = ?;";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ps.setInt(1, s.getCantidad());
            ps.setInt(2, s.getId_producto());
            boolean resul = ps.execute();
            if (resul != true) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Busca y recupera la información de stock asociada a un producto en la
     * base de datos.
     *
     * @param id El ID del producto para el cual se busca el stock.
     * @return Un objeto Stock que contiene la información de stock del
     * producto, o null si no se encuentra.
     */
    @Override
    public Stock searchStock(int id) {
        Stock s = new Stock();
        try {
            this.conectar();
            String query = "select * from stock where id_producto = ?;";
            PreparedStatement ps = this.conexion.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                s.setId(rs.getInt("id"));
                s.setId_producto(rs.getInt("id_producto"));
                s.setCantidad(rs.getInt("cantidad"));
                return s;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
