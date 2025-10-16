import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMenu extends JFrame {
    public static void main(String[] args) {
        ProductMenu menu = new ProductMenu();
        menu.setSize(700, 600);
        menu.setLocationRelativeTo(null);
        menu.setContentPane(menu.mainPanel);
        menu.getContentPane().setBackground(Color.WHITE);
        menu.setVisible(true);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private int selectedIndex = -1;
    private Database database;

    private JPanel mainPanel;
    private JTextField idField;
    private JTextField namaField;
    private JTextField hargaField;
    private JTable productTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox<String> kategoriComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel idLabel;
    private JLabel namaLabel;
    private JLabel hargaLabel;
    private JLabel kategoriLabel;
    private JRadioButton yaRadioButton;
    private JRadioButton tidakRadioButton;

    public ProductMenu() {
        database = new Database();

        productTable.setModel(setTable());

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        String[] kategoriData = {"???", "Elektronik", "Makanan", "Minuman", "Pakaian", "Alat Tulis"};
        kategoriComboBox.setModel(new DefaultComboBoxModel<>(kategoriData));

        deleteButton.setVisible(false);

        addUpdateButton.addActionListener(e -> {
            if (selectedIndex == -1) insertData();
            else updateData();
        });

        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Apakah Anda yakin ingin menghapus data ini?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) deleteData();
        });

        cancelButton.addActionListener(e -> clearForm());

        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectedIndex = productTable.getSelectedRow();
                idField.setText(productTable.getValueAt(selectedIndex, 1).toString());
                namaField.setText(productTable.getValueAt(selectedIndex, 2).toString());
                hargaField.setText(productTable.getValueAt(selectedIndex, 3).toString());
                kategoriComboBox.setSelectedItem(productTable.getValueAt(selectedIndex, 4).toString());
                String curLokal = productTable.getValueAt(selectedIndex, 5).toString();
                if (curLokal.equalsIgnoreCase("Ya")) yaRadioButton.setSelected(true);
                else tidakRadioButton.setSelected(true);

                addUpdateButton.setText("Update");
                deleteButton.setVisible(true);

                idField.setEditable(false);
            }
        });

        ButtonGroup groupLokal = new ButtonGroup();
        groupLokal.add(yaRadioButton);
        groupLokal.add(tidakRadioButton);
        groupLokal.clearSelection();
    }

    public final DefaultTableModel setTable() {
        Object[] cols = {"No", "ID Produk", "Nama", "Harga", "Kategori", "Produk Lokal?"};
        DefaultTableModel tmp = new DefaultTableModel(null, cols);

        try {
            ResultSet rs = database.selectQuery("SELECT * FROM product");
            int i = 1;
            while (rs.next()) {
                Object[] row = new Object[6];
                row[0] = i++;
                row[1] = rs.getString("id");
                row[2] = rs.getString("nama");
                row[3] = rs.getDouble("harga");
                row[4] = rs.getString("kategori");
                boolean lokal = rs.getBoolean("lokal");
                row[5] = lokal ? "Ya" : "Tidak";
                tmp.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return tmp;
    }

    public void insertData() {
        try {
            String id = idField.getText().trim();
            String nama = namaField.getText().trim();
            String hargaText = hargaField.getText().trim();
            String kategori = kategoriComboBox.getSelectedItem().toString();
            boolean lokalDipilih = yaRadioButton.isSelected() || tidakRadioButton.isSelected();
            boolean lokal = yaRadioButton.isSelected();

            // ✅ Validasi input kosong
            if (id.isEmpty() || nama.isEmpty() || hargaText.isEmpty() || kategori.equals("???") || !lokalDipilih) {
                JOptionPane.showMessageDialog(null,
                        "Data belum lengkap!\nSilakan isi semua kolom dan pilih status produk lokal.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double harga = Double.parseDouble(hargaText);

            // ✅ Cek apakah ID sudah ada di database
            ResultSet rs = database.selectQuery("SELECT id FROM product WHERE id='" + id + "'");
            if (rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "ID sudah digunakan! Silakan gunakan ID lain.",
                        "Duplikasi ID", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ✅ Insert ke database
            String sql = "INSERT INTO product (id, nama, harga, kategori, lokal) VALUES (" +
                    "'" + id + "', '" + nama + "', " + harga + ", '" + kategori + "', " + (lokal ? 1 : 0) + ")";
            database.insertUpdateDeleteQuery(sql);

            // Refresh tabel dan reset form
            productTable.setModel(setTable());
            clearForm();

            JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan!");
        }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null,
                    "Harga harus berupa angka!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Terjadi kesalahan saat menambahkan data:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateData() {
        try {
            String id = idField.getText().trim();
            String nama = namaField.getText().trim();
            String hargaText = hargaField.getText().trim();
            String kategori = kategoriComboBox.getSelectedItem().toString();
            boolean lokalDipilih = yaRadioButton.isSelected() || tidakRadioButton.isSelected();
            boolean lokal = yaRadioButton.isSelected();

            // ✅ Validasi input kosong
            if (id.isEmpty() || nama.isEmpty() || hargaText.isEmpty() || kategori.equals("???") || !lokalDipilih) {
                JOptionPane.showMessageDialog(null,
                        "Data belum lengkap!\nSilakan isi semua kolom dan pilih status produk lokal.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double harga = Double.parseDouble(hargaText);

            // ✅ Pastikan data dengan ID ini benar-benar ada
            ResultSet rs = database.selectQuery("SELECT id FROM product WHERE id='" + id + "'");
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null,
                        "Data dengan ID " + id + " tidak ditemukan!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ✅ Update database
            String sql = "UPDATE product SET " +
                    "nama='" + nama + "', " +
                    "harga=" + harga + ", " +
                    "kategori='" + kategori + "', " +
                    "lokal=" + (lokal ? 1 : 0) + " " +
                    "WHERE id='" + id + "'";
            database.insertUpdateDeleteQuery(sql);

            productTable.setModel(setTable());
            clearForm();

            JOptionPane.showMessageDialog(null, "Data berhasil diperbarui!");
        }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null,
                    "Harga harus berupa angka!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Terjadi kesalahan saat memperbarui data:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteData() {
        try {
            if (selectedIndex < 0) {
                JOptionPane.showMessageDialog(null,
                        "Pilih data yang ingin dihapus terlebih dahulu.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String id = idField.getText().trim();

            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Apakah Anda yakin ingin menghapus data dengan ID: " + id + "?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) deleteData();

            // ✅ Hapus dari database
            String sql = "DELETE FROM product WHERE id='" + id + "'";
            database.insertUpdateDeleteQuery(sql);

            productTable.setModel(setTable());
            clearForm();

            JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Terjadi kesalahan saat menghapus data:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void clearForm() {
        idField.setText("");
        namaField.setText("");
        hargaField.setText("");
        kategoriComboBox.setSelectedIndex(0);
        yaRadioButton.setSelected(false);
        tidakRadioButton.setSelected(false);
        addUpdateButton.setText("Add");
        deleteButton.setVisible(false);
        selectedIndex = -1;

        // 🔓 Saat tambah data baru → ID bisa diketik lagi
        idField.setEditable(true);
    }

    private void createUIComponents() {
        // placeholder
    }
}
