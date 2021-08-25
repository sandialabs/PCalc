package gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended;

import java.util.Collection;
import java.util.LinkedHashSet;

import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gnem.dbtabledefs.DBTableDefs;

public class DBTableDefsExtended {

    static public String getVersion() {
        return Utils.getVersion("db-table-defs-extended");
    }

    static public Collection<String> getDependencies() {
        Collection<String> dependencies = new LinkedHashSet<>();
        addDependencies(dependencies);
        return dependencies;
    }

    static public void addDependencies(Collection<String> dependencies) {
        dependencies.add("DBTableDefsExtended " + getVersion());
        dependencies.add("DBTableDefs " + DBTableDefs.getVersion());
        dependencies.add("Utils " + Utils.getVersion());
    }

}
