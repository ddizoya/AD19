/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ad19;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.driver.OracleDriver;

/**
 *
 * @author oracle
 */
public class BaseRelacionalB {

    String driver = "jdbc:oracle:thin:";
    String host = "localhost.localdomain"; // tambien puede ser una ip como "192.168.1.14"
    int porto = 1521;
    String tabla = "produtos";
    String sid = "orcl";
    String usuario = "hr";
    String password = "hr";
    String url = "jdbc:oracle:thin:@localhost:1521:orcl";

    Statement st;
    ResultSet rs;

    public BaseRelacionalB() {
        try {
            DriverManager.deregisterDriver(new OracleDriver());
            System.err.println("*Se ha registrado el Driver de Oracle. ");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Connection conectarse() throws SQLException {
        Connection conn = DriverManager.getConnection(url, usuario, password);
        return conn;

    }

    public void inxerir(String cod, String desc, int prezo) throws SQLException {
        System.out.println(">>Metiendo nueva fila...");
        String consulta = "insert into " + tabla + " values ('" + cod + "','" + desc + "'," + prezo + ")";
        st = conectarse().createStatement();
        st.executeUpdate(consulta);

    }

    public void listar() throws SQLException {
        System.out.println(">>Se procede a consultar la tabla " + tabla);
        String consulta = "Select * from " + tabla;
        st = conectarse().createStatement();
        rs = st.executeQuery(consulta);
        System.out.println("Listado de elementos en " + tabla + ": ");
        recorrer();

    }

    private void recorrer() throws SQLException { //Método para recorrer el ResultSet. 
        while (rs.next()) {
            String cod = rs.getString("cod");
            String des = rs.getString("descricion");
            int prezo = rs.getInt("prezo");
            System.out.println(cod + " " + des + " " + prezo);
        }
    }

    private void RSmodificable() throws SQLException {
        String consulta = "Select cod, descricion, prezo from " + tabla;
        st = conectarse().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        rs = st.executeQuery(consulta);
    }

    public void modificarDentroResultSet(String cod, int valor) throws SQLException {
        RSmodificable();
        while (rs.next()) {
            String aux = rs.getString("cod");
            if (aux.equalsIgnoreCase(cod)) {
                rs.updateInt("prezo", valor); //Modificamos el valor de precio en la fila que cod=="p2"
                rs.updateRow(); //Hacemos commit de los cambios realizados.
                System.out.println("Tabla con los nuevos valores modificados: ");
                rs.first(); //Volvemos al principio ya que nos hemos movido a lo largo del ResultSet.
                rs.previous(); //Volvemos a la fila 0 (Origen), ya que sino nos saltamos la fila 1 al recorrer con while-next.
                break;//Salimos del bucle;
            }
        }
        recorrer(); //Usamos el método precreado que recorre los valor del RS y lo imprime.

    }

    public void inxerirRS(String cod, String des, int prezo) throws SQLException {
        RSmodificable();

        rs.moveToInsertRow(); //Iniciamos la inserción de la nueva fila.
        rs.updateString("cod", cod);
        rs.updateString("descricion", des);
        rs.updateInt("prezo", prezo);
        rs.insertRow(); //Commit de la nueva fila insertada.

        System.err.println("Nuevo produtos insertado: ");
        listar(); //Listamos de nuevo todo el contenido de la tabla.

    }

    public void borrarProduto(String cod) throws SQLException {
        RSmodificable();
        while (rs.next()) {
            if (rs.getString("cod").equalsIgnoreCase(cod)) { //Si la fila tiene cod == "p3", borramos la fila. 
                rs.deleteRow();
                break;
            }
        }
        listar(); //Listamos de nuevo el contenido, y vemos que se ha borrado el produto p3
    }

    public static void main(String[] args) {
        try {
            BaseRelacionalB obj = new BaseRelacionalB();
            obj.inxerir("p1", "parafusos", 3);
            obj.inxerir("p2", "cravos", 4);
            obj.inxerir("p3", "tachas", 6);

            obj.listar();
            obj.modificarDentroResultSet("p2", 8);
            obj.inxerirRS("p4", "martelo", 20);
            obj.borrarProduto("p3");

        } catch (SQLException ex) {
            Logger.getLogger(BaseRelacionalB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
