package com.p2p.domain.user;

public class User {
    private UserId id;
    private String nama;
    private String email;
    private String password;
    private int usia;
    private int role;

    public User(String nama, String email, String password, int usia, int role) {
        if (usia < 18) {
            throw new IllegalArgumentException("Usia minimal untuk mendaftar adalah 18 tahun");
        }
        if (role != 1 && role != 2 && role != 3) {
            throw new IllegalArgumentException("Role pengguna tidak valid");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password minimal harus 8 karakter");
        }
        this.id = new UserId(role);
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.usia = usia;
        this.role = role;
    }

    public UserId getId() { return id; }
    public void setId(UserId id) {
        this.id = id;
    }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public int getUsia() { return usia; }
    public int getRole() { return role; }
    
    public void setNama(String nama) { this.nama = nama; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setUsia(int usia) { this.usia = usia; }
    public void setRole(int role) { this.role = role; }
}