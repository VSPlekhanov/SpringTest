package test.spring.model;

public class Client {
    private int id;
    private String fullName;

    Client(int id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Client() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullname(String fullName) {
        this.fullName = fullName;
    }

}
