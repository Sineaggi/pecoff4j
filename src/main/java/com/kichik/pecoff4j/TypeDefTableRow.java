package com.kichik.pecoff4j;

public class TypeDefTableRow {
    private int flags;
    private int typeName;
    private int typeNamespace;
    private int extendsType;
    private int fieldList;
    private int methodList;

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
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

    public int getExtendsType() {
        return extendsType;
    }

    public void setExtendsType(int extendsType) {
        this.extendsType = extendsType;
    }

    public int getFieldList() {
        return fieldList;
    }

    public void setFieldList(int fieldList) {
        this.fieldList = fieldList;
    }

    public int getMethodList() {
        return methodList;
    }

    public void setMethodList(int methodList) {
        this.methodList = methodList;
    }
}
