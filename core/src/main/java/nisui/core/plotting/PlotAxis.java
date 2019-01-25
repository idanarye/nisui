package nisui.core.plotting;

public class PlotAxis {
	private String caption;
	private ScaleType scaleType;
	private String unitName;
	private String expression;

	public PlotAxis(String caption, ScaleType scaleType, String unitName, String expression) {
		this.caption = caption;
		this.scaleType = scaleType;
		this.unitName = unitName;
		this.expression = expression;
	}

	/**
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
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

	@Override
	public String toString() {
		return String.format(
				"%s(%s, scale=%s, unit=%s, expression=%s)",
				getClass().getSimpleName(),
				caption, scaleType, unitName, expression);
	}

	public String getTextForPlot() {
		if (caption != null && !caption.isEmpty()) {
			return caption;
		}
		return expression;
	}
}
