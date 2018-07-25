package nisui.core.plotting;

import java.util.List;

public class PlotEntry {
	private String name;
	private List<PlotAxis> axes;
	private List<PlotFilter> filters;
	private List<PlotFormula> formulas;

	public PlotEntry(String name, List<PlotAxis> axes, List<PlotFilter> filters, List<PlotFormula> formulas) {
		this.setName(name);
		this.setAxes(axes);
		this.setFilters(filters);
		this.setFormulas(formulas);
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
}
