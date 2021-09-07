package com.kichik.pecoff4j;

public enum ResolutionScope {
    module,
    moduleRef,
    assemblyRef,
    typeRef,
    ;

    public static interface Value {
        public static class ModuleValue implements Value {
            private final ModuleTableRow module;

            public ModuleValue(ModuleTableRow module) {
                this.module = module;
            }

            public ModuleTableRow module() {
                return module;
            }
        };
        public static class ModuleRefValue implements Value {
            private final ModuleRefTableRow moduleRef;

            public ModuleRefValue(ModuleRefTableRow module) {
                this.moduleRef = module;
            }

            public ModuleRefTableRow moduleRef() {
                return moduleRef;
            }
        };
        public static class AssemblyRefValue implements Value {
            private final AssemblyRefTableRow assemblyRef;

            public AssemblyRefValue(AssemblyRefTableRow assemblyRef) {
                this.assemblyRef = assemblyRef;
            }

            public AssemblyRefTableRow assemblyRef() {
                return assemblyRef;
            }
        };
        public static class TypeRefValue implements Value {
            private final TypeRefTableRow typeRef;

            public TypeRefValue(TypeRefTableRow typeRef) {
                this.typeRef = typeRef;
            }

            public TypeRefTableRow typeRef() {
                return typeRef;
            }
        };
    }
}
