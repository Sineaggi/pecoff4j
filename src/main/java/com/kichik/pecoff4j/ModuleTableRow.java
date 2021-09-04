package com.kichik.pecoff4j;

public class ModuleTableRow {
    private int generation;//
    private int name;// { get; set; }
    private int mvid;// { get; set; }
    private int encId;// { get; set; }
    private int encBaseId;// { get; set; }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getMvid() {
        return mvid;
    }

    public void setMvid(int mvid) {
        this.mvid = mvid;
    }

    public int getEncId() {
        return encId;
    }

    public void setEncId(int encId) {
        this.encId = encId;
    }

    public int getEncBaseId() {
        return encBaseId;
    }

    public void setEncBaseId(int encBaseId) {
        this.encBaseId = encBaseId;
    }
}
