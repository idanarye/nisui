package nisui.java_runner;

import org.junit.*;
import org.assertj.core.api.Assertions;

import nisui.core.ExperimentValuesHandler;

class ValueClass {
	public int a;
	public double b;
	public String c;
}

public class JavaValuesTest {
	@Test
	public void writeAndRead() {
		ExperimentValuesHandler<ValueClass> handler = new JavaExperimentValuesHandler<>(ValueClass.class);
		ValueClass value = new ValueClass();
		value.a = 1;
		value.b = 2.5;
		value.c = "3";

		Assertions.assertThat(handler.field("a").get(value)).isEqualTo(1);
		Assertions.assertThat(handler.field("b").get(value)).isEqualTo(2.5);
		Assertions.assertThat(handler.field("c").get(value)).isEqualTo("3");

		value = handler.createValue();

		handler.field("a").set(value, 4);
		handler.field("b").set(value, 5.5);
		handler.field("c").set(value, "6");

		Assertions.assertThat(value.a).isEqualTo(4);
		Assertions.assertThat(value.b).isEqualTo(5.5);
		Assertions.assertThat(value.c).isEqualTo("6");
	}
}
