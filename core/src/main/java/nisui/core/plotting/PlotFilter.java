package nisui.core.plotting;

public class PlotFilter {
	private String caption;
	private FilterType filterType;
	private String unitName;
	private String expression;

	public PlotFilter(String caption, FilterType filterType, String unitName, String expression) {
		this.setCaption(caption);
		this.setFilterType(filterType);
		this.setUnitName(unitName);
		this.setExpression(expression);
	}

	/**
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * @return the unitName
	 */
	public String getUnitName() {
		return unitName;
	}

	/**
	 * @param unitName the unitName to set
	 */
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	/**
	 * @return the filterType
	 */
	public FilterType getFilterType() {
		return filterType;
	}

	/**
	 * @param filterType the filterType to set
	 */
	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}

	/**
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @param caption the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

}
