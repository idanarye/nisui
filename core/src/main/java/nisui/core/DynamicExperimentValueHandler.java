package nisui.core;

import java.util.Collection;
import java.util.LinkedHashMap;

public class DynamicExperimentValueHandler extends ExperimentValuesHandler<DynamicExperimentValue> {
	private LinkedHashMap<String, DynamicField> fields;

	public DynamicExperimentValueHandler() {
		this.fields = new LinkedHashMap<>();
	}

	public class DynamicField extends Field {
		private int index;
		private String name;
		private Class<?> type;

		private DynamicField(int index, String name, Class type) {
			this.index = index;
			this.name = name;
			this.type = type;
		}

		@Override
		public Object get(DynamicExperimentValue dynamicExperimentValue) {
			return dynamicExperimentValue.get(index);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<?> getType() {
			return type;
		}

		@Override
		public void set(DynamicExperimentValue dynamicExperimentValue, Object value) {
			dynamicExperimentValue.set(index, value);
		}

	}

	@Override
	public DynamicExperimentValue createValue() {
		return new DynamicExperimentValue(this);
	}

	@Override
	public DynamicField field(String name) {
		return fields.get(name);
	}

	@Override
	public Collection<DynamicField> fields() {
		return fields.values();
	}

	public DynamicExperimentValueHandler addField(String name, Class<?> type) {
		fields.put(name, new DynamicField(fields.size(), name, type));
		return this;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		for (DynamicField field : fields.values()) {
			if (0 < field.index) {
				result.append(",\n");
			} else {
				result.append("\n");
			}
			result.append('\t').append(field.name).append(": ").append(field.type);
		}
		if (!fields.isEmpty()) {
			result.append('\n');
		}
		result.append('}');
		return result.toString();
	}
}
