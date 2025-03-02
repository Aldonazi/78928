package com.mycompany.ticketcine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CompraBoletosApp {
    private JFrame frame;
    private JComboBox<String> peliculasComboBox;
    private JComboBox<String> salasComboBox;
    private JPanel asientosPanel;
    private Map<Integer, JButton> asientosBotones;

    public CompraBoletosApp() {
        frame = new JFrame("Compra de Boletos");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        peliculasComboBox = new JComboBox<>();
        salasComboBox = new JComboBox<>();
        topPanel.add(peliculasComboBox);
        topPanel.add(salasComboBox);
        frame.add(topPanel, BorderLayout.NORTH);

        asientosPanel = new JPanel(new GridLayout(5, 5, 5, 5));
        frame.add(asientosPanel, BorderLayout.CENTER);

        cargarPeliculas();
        cargarSalas();

        salasComboBox.addActionListener(e -> cargarAsientos());

        frame.setVisible(true);
    }

    private void cargarPeliculas() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT nombre FROM peliculas");
            while (resultSet.next()) {
                peliculasComboBox.addItem(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarSalas() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT nombre FROM salas");
            while (resultSet.next()) {
                salasComboBox.addItem(resultSet.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarAsientos() {
        asientosPanel.removeAll();
        asientosBotones = new HashMap<>();

        String salaSeleccionada = (String) salasComboBox.getSelectedItem();
        if (salaSeleccionada != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                int idSala = obtenerIdSala(connection, salaSeleccionada);
                String query = "SELECT id_asiento, numero_asiento, estado FROM asientos WHERE id_sala = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, idSala);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            int idAsiento = resultSet.getInt("id_asiento");
                            int numeroAsiento = resultSet.getInt("numero_asiento");
                            boolean disponible = "disponible".equals(resultSet.getString("estado"));

                            JButton asientoButton = new JButton(String.valueOf(numeroAsiento));
                            asientoButton.setEnabled(disponible);
                            asientoButton.addActionListener(e -> comprarBoleto(idAsiento, numeroAsiento));
                            
                            asientosBotones.put(idAsiento, asientoButton);
                            asientosPanel.add(asientoButton);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al cargar los asientos: " + e.getMessage());
            }
        }
        asientosPanel.revalidate();
        asientosPanel.repaint();
    }

    private void comprarBoleto(int idAsiento, int numeroAsiento) {
        String peliculaSeleccionada = (String) peliculasComboBox.getSelectedItem();
        String salaSeleccionada = (String) salasComboBox.getSelectedItem();
        
        if (peliculaSeleccionada != null && salaSeleccionada != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                int idPelicula = obtenerIdPelicula(connection, peliculaSeleccionada);

                String query = "INSERT INTO ventas_boletos (id_asiento, id_pelicula) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, idAsiento);
                    statement.setInt(2, idPelicula);
                    statement.executeUpdate();
                }

                String updateQuery = "UPDATE asientos SET estado = 'vendido' WHERE id_asiento = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, idAsiento);
                    updateStatement.executeUpdate();
                }
                
                JOptionPane.showMessageDialog(frame, "Boleto comprado para " + peliculaSeleccionada + 
                        " en " + salaSeleccionada + " (Asiento: " + numeroAsiento + ")");
                
                JButton asientoButton = asientosBotones.get(idAsiento);
                if (asientoButton != null) {
                    asientoButton.setEnabled(false);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al comprar el boleto: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Por favor, selecciona una película y sala.");
        }
    }

    private int obtenerIdPelicula(Connection connection, String nombrePelicula) throws SQLException {
        String query = "SELECT id_pelicula FROM peliculas WHERE nombre = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nombrePelicula);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_pelicula");
                } else {
                    throw new SQLException("No se encontró la película: " + nombrePelicula);
                }
            }
        }
    }

    private int obtenerIdSala(Connection connection, String nombreSala) throws SQLException {
        String query = "SELECT id_sala FROM salas WHERE nombre = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nombreSala);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_sala");
                } else {
                    throw new SQLException("No se encontró la sala: " + nombreSala);
                }
            }
        }
    }
}
