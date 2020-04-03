package com.neu.community.entity;

public class Labels {

    private int id;
    private String label;
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Labels{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", status=" + status +
                '}';
    }
}
