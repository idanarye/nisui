package nisui.core;

public class DynamicExperimentValue {
	private DynamicExperimentValueHandler handler;
	private Object[] fields;

	DynamicExperimentValue(DynamicExperimentValueHandler handler) {
		this.handler = handler;
		this.fields = new Object[handler.fields().size()];
	}

	Object get(int index) {
		return fields[index];
	}

	public Object get(String name) {
		return handler.field(name).get(this);
	}

	void set(int index, Object value) {
		fields[index] = value;
	}

	public void set(String name, Object value) {
		handler.field(name).set(this, value);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		boolean wroteFirst = false;
		for (DynamicExperimentValueHandler.DynamicField field : handler.fields()) {
			if (wroteFirst) {
				result.append(",\n");
			} else {
				result.append("\n");
				wroteFirst = true;
			}
			result.append('\t').append(field.getName()).append(": ").append(field.get(this));
		}
		if (wroteFirst) { // meaning anything was written
			result.append('\n');
		}
		result.append('}');
		return result.toString();
	}
}
