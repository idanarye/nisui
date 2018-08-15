package nisui.core.plotting;

import java.util.ArrayList;
import java.util.List;

public class PlotEntry {
    private String key;
    private String name;
    private List<PlotAxis> axes;
    private List<PlotFilter> filters;
    private List<PlotFormula> formulas;

    public PlotEntry(String key, String name, List<PlotAxis> axes, List<PlotFilter> filters, List<PlotFormula> formulas) {
        this.setKey(key);
        this.setName(name);
        this.setAxes(axes);
        this.setFilters(filters);
        this.setFormulas(formulas);
    }

    public static PlotEntry buildNew(String name, Object... children) {
        ArrayList<PlotAxis> axes = new ArrayList<>();
        ArrayList<PlotFilter> filters = new ArrayList<>();
        ArrayList<PlotFormula> formulas = new ArrayList<>();

        for (Object child : children) {
            if (child instanceof PlotAxis) {
                axes.add((PlotAxis)child);
            } else if (child instanceof PlotFilter) {
                filters.add((PlotFilter)child);
            } else if (child instanceof PlotFormula) {
                formulas.add((PlotFormula)child);
            } else {
                throw new Error(String.format("Expected Plot info field, not %s", child));
            }
        }

        return new PlotEntry(null, name, axes, filters, formulas);
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the formulas
     */
    public List<PlotFormula> getFormulas() {
        return formulas;
    }

    /**
     * @param formulas the formulas to set
     */
    public void setFormulas(List<PlotFormula> formulas) {
        this.formulas = formulas;
    }

    /**
     * @return the filters
     */
    public List<PlotFilter> getFilters() {
        return filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(List<PlotFilter> filters) {
        this.filters = filters;
    }

    /**
     * @return the axes
     */
    public List<PlotAxis> getAxes() {
        return axes;
    }

    /**
     * @param axes the axes to set
     */
    public void setAxes(List<PlotAxis> axes) {
        this.axes = axes;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    private static void addItemsToStringBuilder(StringBuilder builder, Iterable<?> items) {
        for (Object item : items) {
            builder.append(", ").append(item);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append('(');
        builder.append(getKey());
        builder.append(", ").append(getName());

        addItemsToStringBuilder(builder, axes);
        addItemsToStringBuilder(builder, filters);
        addItemsToStringBuilder(builder, formulas);

        builder.append(')');
        return builder.toString();
    }
}
