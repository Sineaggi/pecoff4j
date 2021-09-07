package com.kichik.pecoff4j;

import java.util.Objects;

public class TypeDefTableRow {
    private final Metadata metadata;
    private int flags;
    private int typeName;
    private int typeNamespace;
    private int extendsType;
    private int fieldList;
    private int methodList;

    public TypeDefTableRow(Metadata metadata) {
        this.metadata = Objects.requireNonNull(metadata);
    }

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

    public String name() {
        return metadata.stringsStream.get(typeName);
    }

    public String namespace() {
        return metadata.stringsStream.get(typeNamespace);
    }

    public boolean isEnum() {
        TypeDefOrRef row = extendsType();
        if (row instanceof TypeDefOrRef.TypeRef) {
            TypeRefTableRow tr = ((TypeDefOrRef.TypeRef)row).typeRef();
            return tr.name().equals("Enum") && tr.namespace().equals("System");
        } else if (row instanceof TypeDefOrRef.TypeDef) {
            TypeDefTableRow td = ((TypeDefOrRef.TypeDef)row).typeDef();
            return td.name().equals("Enum") && td.namespace().equals("System");
        } else {
            throw new RuntimeException("not yet implemented");
        }
    }

    public TypeDefOrRef extendsType() {
        return metadata.typeDefOrRef(extendsType);
    }
}

// todo: replace with sealed hierarchy
interface TypeDefOrRef {
    // todo: replace with record
    class TypeDef implements TypeDefOrRef {
        TypeDefTableRow typeDefTableRow;
        public TypeDef(TypeDefTableRow typeDefTableRow) {
            this.typeDefTableRow = typeDefTableRow;
        }
        public TypeDefTableRow typeDef() {
            return typeDefTableRow;
        }
    }
    // todo: replace with record
    class TypeRef implements TypeDefOrRef {
        TypeRefTableRow typeRefTableRow;
        public TypeRef(TypeRefTableRow typeRefTableRow) {
            this.typeRefTableRow = typeRefTableRow;
        }
        public TypeRefTableRow typeRef() {
            return typeRefTableRow;
        }
    }

    // todo: implement
    class TypeSpec implements TypeDefOrRef {
        public TypeSpec() {
            throw new RuntimeException("not yet implemented");
        }
    }
}
