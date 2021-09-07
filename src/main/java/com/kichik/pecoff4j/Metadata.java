package com.kichik.pecoff4j;

import java.util.List;

public class Metadata {
    private List<ModuleTableRow> moduleTableRows;
    private List<TypeRefTableRow> typeRefTableRows;
    private List<TypeDefTableRow> typeDefTableRows;
    private List<FieldTableRow> fieldTableRows;
    private List<MethodDefTableRow> methodDefTableRows;
    private List<ParamTableRow> paramTableRows;
    private List<InterfaceImplTableRow> interfaceImplTableRows;
    private List<MemberRefTableRow> memberRefTableRows;
    private List<ConstantTableRow> constantTableRows;
    private List<CustomAttributeTableRow> customAttributeTableRows;
    private List<ClassLayoutTableRow> classLayoutTableRows;
    private List<FieldLayoutTableRow> fieldLayoutTableRows;
    private List<ModuleRefTableRow> moduleRefTableRows;
    private List<ImplMapTableRow> implMapTableRows;
    private List<AssemblyTableRow> assemblyTableRows;
    private List<AssemblyRefTableRow> assemblyRefTableRows;
    private List<NestedClassTableRow> nestedClassTableRows;

    protected StringsStream stringsStream;

    public List<ModuleTableRow> getModuleTableRows() {
        return moduleTableRows;
    }

    public void setModuleTableRows(List<ModuleTableRow> moduleTableRows) {
        this.moduleTableRows = moduleTableRows;
    }

    public List<TypeRefTableRow> getTypeRefTableRows() {
        return typeRefTableRows;
    }

    public void setTypeRefTableRows(List<TypeRefTableRow> typeRefTableRows) {
        this.typeRefTableRows = typeRefTableRows;
    }

    public List<TypeDefTableRow> getTypeDefTableRows() {
        return typeDefTableRows;
    }

    public void setTypeDefTableRows(List<TypeDefTableRow> typeDefTableRows) {
        this.typeDefTableRows = typeDefTableRows;
    }

    public List<FieldTableRow> getFieldTableRows() {
        return fieldTableRows;
    }

    public void setFieldTableRows(List<FieldTableRow> fieldTableRows) {
        this.fieldTableRows = fieldTableRows;
    }

    public List<MethodDefTableRow> getMethodDefTableRows() {
        return methodDefTableRows;
    }

    public void setMethodDefTableRows(List<MethodDefTableRow> methodDefTableRows) {
        this.methodDefTableRows = methodDefTableRows;
    }

    public List<ParamTableRow> getParamTableRows() {
        return paramTableRows;
    }

    public void setParamTableRows(List<ParamTableRow> paramTableRows) {
        this.paramTableRows = paramTableRows;
    }

    public List<InterfaceImplTableRow> getInterfaceImplTableRows() {
        return interfaceImplTableRows;
    }

    public void setInterfaceImplTableRows(List<InterfaceImplTableRow> interfaceImplTableRows) {
        this.interfaceImplTableRows = interfaceImplTableRows;
    }

    public List<MemberRefTableRow> getMemberRefTableRows() {
        return memberRefTableRows;
    }

    public void setMemberRefTableRows(List<MemberRefTableRow> memberRefTableRows) {
        this.memberRefTableRows = memberRefTableRows;
    }

    public List<ConstantTableRow> getConstantTableRows() {
        return constantTableRows;
    }

    public void setConstantTableRows(List<ConstantTableRow> constantTableRows) {
        this.constantTableRows = constantTableRows;
    }

    public List<CustomAttributeTableRow> getCustomAttributeTableRows() {
        return customAttributeTableRows;
    }

    public void setCustomAttributeTableRows(List<CustomAttributeTableRow> customAttributeTableRows) {
        this.customAttributeTableRows = customAttributeTableRows;
    }

    public List<ClassLayoutTableRow> getClassLayoutTableRows() {
        return classLayoutTableRows;
    }

    public void setClassLayoutTableRows(List<ClassLayoutTableRow> classLayoutTableRows) {
        this.classLayoutTableRows = classLayoutTableRows;
    }

    public List<FieldLayoutTableRow> getFieldLayoutTableRows() {
        return fieldLayoutTableRows;
    }

    public void setFieldLayoutTableRows(List<FieldLayoutTableRow> fieldLayoutTableRows) {
        this.fieldLayoutTableRows = fieldLayoutTableRows;
    }

    public List<ModuleRefTableRow> getModuleRefTableRows() {
        return moduleRefTableRows;
    }

    public void setModuleRefTableRows(List<ModuleRefTableRow> moduleRefTableRows) {
        this.moduleRefTableRows = moduleRefTableRows;
    }

    public List<ImplMapTableRow> getImplMapTableRows() {
        return implMapTableRows;
    }

    public void setImplMapTableRows(List<ImplMapTableRow> implMapTableRows) {
        this.implMapTableRows = implMapTableRows;
    }

    public List<AssemblyTableRow> getAssemblyTableRows() {
        return assemblyTableRows;
    }

    public void setAssemblyTableRows(List<AssemblyTableRow> assemblyTableRows) {
        this.assemblyTableRows = assemblyTableRows;
    }

    public List<AssemblyRefTableRow> getAssemblyRefTableRows() {
        return assemblyRefTableRows;
    }

    public void setAssemblyRefTableRows(List<AssemblyRefTableRow> assemblyRefTableRows) {
        this.assemblyRefTableRows = assemblyRefTableRows;
    }

    public List<NestedClassTableRow> getNestedClassTableRows() {
        return nestedClassTableRows;
    }

    public void setNestedClassTableRows(List<NestedClassTableRow> nestedClassTableRows) {
        this.nestedClassTableRows = nestedClassTableRows;
    }

    public TypeDefOrRef typeDefOrRef(int extendsType) {
        int peko = compositeIndexSize(List.of(typeDefTableRows.size(), typeRefTableRows.size()));

        //int a = compositeValue();
        //int total = typeDefTableRows.size() + typeRefTableRows.size(); // todo: + typeSpecTableRows.size();
        throw new RuntimeException("not impled");
    }


    private int bitsNeeded(int rc)
    {
        int r = 1;
        --rc;
        while ((rc >>= 1) != 0)
            ++r;
        return r;
    }

    /*
    static class CompositeIndex {
        private final int codedIndex;
        public CompositeIndex(int codedIndex) {
            this.codedIndex = codedIndex;
        }
        public int index() {
            return codedIndex >> indexBits;
        }

        public MD type() {

        }
    }

    public ResolutionScope.Value compositeValue(Metadata db, CompositeIndex c) {
        if (j instanceof ResolutionScope) {
             switch (c.type())
            {
                case ResolutionScope.module:
                    return new ResolutionScope.Value.ModuleValue(moduleTableRows.get(c.index()));
                    //return ResolutionScopeValue(db.moduleTable[c.index]);
                case ResolutionScope.moduleRef:
                    return new ResolutionScope.Value.ModuleRefValue(moduleRefTableRows.get(c.index()));
                    //return ResolutionScopeValue(db.moduleRefTable[c.index]);
                case ResolutionScope.assemblyRef:
                    return new ResolutionScope.Value.AssemblyRefValue(assemblyRefTableRows.get(c.index()));
                    //return ResolutionScopeValue(db.assemblyRefTable[c.index]);
                case ResolutionScope.typeRef:
                    return new ResolutionScope.Value.TypeRefValue(typeRefTableRows.get(c.index()));
                    //return ResolutionScopeValue(db.typeRefTable[c.index]);
            }
        }
    }
     */

    private int bitsNeeded(List<Integer> rowCounts)
    {

        //int t1 = bitsNeeded(rowCounts.size() > 0 ? rowCounts.get(0) : 0);
        //int t2 = bitsNeeded(rowCounts.subList(1, rowCounts.size()));
        //return Math.max(t1, t2);
        int max = 0;
        for (int rc : rowCounts) {
            max = Math.max(max, bitsNeeded(rc));
        }
        return max;
    }


    private int compositeIndexSize(List<Integer> rowCounts)
    {

        // 2^(16 - log2 3);
        //rowCounts.size();
        int total = rowCounts.stream().mapToInt(i -> i).sum();
        int maxWeCanEncode = (int)Math.pow(2, (16 - (Math.log(rowCounts.size()) / Math.log(2))));

        return (bitsNeeded(rowCounts) + bitsNeeded(rowCounts.size()) <= 16) ? 2 : 4;
    }
}
