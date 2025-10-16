public class Product {
    private String id;
    private String nama;
    private double harga;
    private String kategori;
    private boolean lokal;

    public Product(String id, String nama, double harga, String kategori, boolean lokal) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.kategori = kategori;
        this.lokal = lokal;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public void setLokal(boolean lokal) { this.lokal = lokal;}

    public String getId() {
        return this.id;
    }

    public String getNama() {
        return this.nama;
    }

    public double getHarga() {
        return this.harga;
    }

    public String getKategori() {
        return this.kategori;
    }

    public boolean getLokal() { return lokal;}
}