package com.thangtv.ipsapp.models;

/**
 * Created by tranvietthang on 12/17/16.
 */

public class KNNRecord {
    private float x = 0;
    private float y = 0;
    private float z = 0;
    private float distance = 0;
    private String position = "";

    public KNNRecord(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void calculateDistanceTo(KNNRecord record) {
        float dx = this.x - record.getX();
        float dy = this.y - record.getY();
        float dz = this.z - record.getZ();

        this.distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
