package nisui.h2_store;

import nisui.core.ExperimentValuesHandler;

public class H2SingleFieldGlue {
    private H2FieldDefinition sqlField;
    private ExperimentValuesHandler objField;

    H2SingleFieldGlue(H2FieldDefinition sqlField, ExperimentValuesHandler objField) {
        this.sqlField = sqlField;
        this.objField = objField;
    }
}
