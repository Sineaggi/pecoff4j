package com.kichik.pecoff4j;

public class TypeRefTableRow {
    private int resolutionScope;
    private int typeName;
    private int typeNamespace;

    public int getResolutionScope() {
        return resolutionScope;
    }

    public void setResolutionScope(int resolutionScope) {
        this.resolutionScope = resolutionScope;
    }

    public int getTypeName() {
        return typeName;
    }

    public void setTypeName(int typeName) {
        this.typeName = typeName;
    }

    public int getTypeNamespace() {
        return typeNamespace;
    }

    public void setTypeNamespace(int typeNamespace) {
        this.typeNamespace = typeNamespace;
    }
}
