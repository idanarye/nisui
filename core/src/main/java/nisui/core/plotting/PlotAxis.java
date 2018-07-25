package nisui.core.plotting;

public class PlotAxis {
	private String caption;
	private ScaleType scaleType;
	private String unitName;
	private String expression;
	private Double min;
	private Double max;

	public PlotAxis(String caption, ScaleType scaleType, String unitName, String expression, Double min, Double max) {
		this.caption = caption;
		this.scaleType = scaleType;
		this.unitName = unitName;
		this.expression = expression;
		this.min = min;
		this.max = max;
	}

	/**
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}
	/**
	 * @return the max
	 */
	public Double getMax() {
		return max;
	}
	/**
	 * @param max the max to set
	 */
	public void setMax(Double max) {
		this.max = max;
	}
	/**
	 * @return the min
	 */
	public Double getMin() {
		return min;
	}
	/**
	 * @param min the min to set
	 */
	public void setMin(Double min) {
		this.min = min;
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
	 * @return the scaleType
	 */
	public ScaleType getScaleType() {
		return scaleType;
	}
	/**
	 * @param scaleType the scaleType to set
	 */
	public void setScaleType(ScaleType scaleType) {
		this.scaleType = scaleType;
	}
	/**
	 * @param caption the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}
}
