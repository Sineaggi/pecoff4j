package com.kichik.pecoff4j;

public class ClassLayoutTableRow {
    private int packingSize;
    private int classSize;
    private int parent;

    public int getPackingSize() {
        return packingSize;
    }

    public void setPackingSize(int packingSize) {
        this.packingSize = packingSize;
    }

    public int getClassSize() {
        return classSize;
    }

    public void setClassSize(int classSize) {
        this.classSize = classSize;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }
}
