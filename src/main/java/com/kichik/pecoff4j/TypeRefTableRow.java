package com.kichik.pecoff4j;

import java.util.Objects;

public class TypeRefTableRow {
    private Metadata metadata;
    private int resolutionScope;
    private int typeName;
    private int typeNamespace;

    public TypeRefTableRow(Metadata md) {
        this.metadata = Objects.requireNonNull(md);
    }

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

    public String name() {
        return metadata.stringsStream.get(typeName);
    }

    public String namespace() {
        return metadata.stringsStream.get(typeNamespace);
    }

    public boolean isEnum() {
        return name().equals("Enum") && namespace().equals("System");
    }
}
