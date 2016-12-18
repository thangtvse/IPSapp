package com.thangtv.ipsapp.models;

import java.util.Date;

/**
 * Created by tranvietthang on 12/16/16.
 */

public class Record {
    private Date createdAt;
    private float magX;
    private float magY;
    private float magZ;
    private float wmagX;
    private float wmagY;
    private float wmagZ;
    private String collector;
    private String studentCode;
    private String group;
    private String position;


    public Record(Date createdAt, float magX, float magY, float magZ, float wmagX, float wmagY, float wmagZ, String collector, String studentCode, String group, String position) {
        this.createdAt = createdAt;
        this.magX = magX;
        this.magY = magY;
        this.magZ = magZ;
        this.wmagX = wmagX;
        this.wmagY = wmagY;
        this.wmagZ = wmagZ;
        this.collector = collector;
        this.studentCode = studentCode;
        this.group = group;
        this.position = position;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public float getMagX() {
        return magX;
    }

    public void setMagX(float magX) {
        this.magX = magX;
    }

    public float getMagY() {
        return magY;
    }

    public void setMagY(float magY) {
        this.magY = magY;
    }

    public float getMagZ() {
        return magZ;
    }

    public void setMagZ(float magZ) {
        this.magZ = magZ;
    }

    public float getWmagX() {
        return wmagX;
    }

    public void setWmagX(float wmagX) {
        this.wmagX = wmagX;
    }

    public float getWmagY() {
        return wmagY;
    }

    public void setWmagY(float wmagY) {
        this.wmagY = wmagY;
    }

    public float getWmagZ() {
        return wmagZ;
    }

    public void setWmagZ(float wmagZ) {
        this.wmagZ = wmagZ;
    }

    public String getCollector() {
        return collector;
    }

    public void setCollector(String collector) {
        this.collector = collector;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
