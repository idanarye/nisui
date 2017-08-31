package nisui.core;

import org.junit.*;
import org.assertj.core.api.Assertions;

public class DynamicValuesTest {
	@Test
	public void writeAndRead() {
		DynamicExperimentValueHandler handler = new DynamicExperimentValueHandler()
			.addField("a", int.class)
			.addField("b", double.class)
			.addField("c", String.class);

		Object[] fieldValues = new Object[]{1, 2.5, "3"};
		DynamicExperimentValue value = handler.createValue();

		value.set("a", 1);
		value.set("b", 2.5);
		value.set("c", "3");

		int zipExistedForAgesButJavaStreamsDoesntHaveIt = 0;
		for (ExperimentValuesHandler<DynamicExperimentValue>.Field field : handler.fields()) {
			Assertions.assertThat(field.get(value)).isEqualTo(fieldValues[zipExistedForAgesButJavaStreamsDoesntHaveIt]);
			++zipExistedForAgesButJavaStreamsDoesntHaveIt;
		}

		fieldValues = new Object[]{4, 5.5, "6"};
		value = handler.createValue();
		zipExistedForAgesButJavaStreamsDoesntHaveIt = 0;
		for (ExperimentValuesHandler<DynamicExperimentValue>.Field field : handler.fields()) {
			field.set(value, fieldValues[zipExistedForAgesButJavaStreamsDoesntHaveIt]);
			++zipExistedForAgesButJavaStreamsDoesntHaveIt;
		}

		Assertions.assertThat(value.get("a")).isEqualTo(4);
		Assertions.assertThat(value.get("b")).isEqualTo(5.5);
		Assertions.assertThat(value.get("c")).isEqualTo("6");
	}
}

