package nisui.core.plotting;

public class PlotFormula {
	private String caption;
	private String symbol;
	private ScaleType scaleType;
	private String unitName;
	private String expression;

	public PlotFormula(String caption, String symbol, ScaleType scaleType, String unitName, String expression) {
		this.setCaption(caption);
		this.setSymbol(symbol);
		this.setScaleType(scaleType);
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
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * @param symbol the symbol to set
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
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

	@Override
	public String toString() {
		return String.format(
				"%s(%s, symbol=%s, scaleType=%s, unit=%s, expression=%s)",
				getClass().getSimpleName(),
				caption, symbol, scaleType, unitName, expression);
	}
}
