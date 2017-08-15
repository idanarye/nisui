package nisui.java_runner;

import lombok.*;
import nisui.core.ExperimentValuesHandler;
import org.junit.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ValueClass {
	private int a;
	private double b;
	private String c;
}

public class JavaValuesTest {
	@Test
	public void writeAndRead() {
		ExperimentValuesHandler<ValueClass> handler = new JavaExperimentValuesHandler<>(ValueClass.class);
		ValueClass value = new ValueClass(1, 2.5, "3");

		assert handler.field("a").get(value).equals(1);
		assert handler.field("b").get(value).equals(2.5);
		assert handler.field("c").get(value).equals("3");

		value = handler.createValue();

		handler.field("a").set(value, 4);
		handler.field("b").set(value, 5.5);
		handler.field("c").set(value, "6");

		assert value.getA() == 4;
		assert value.getB() == 5.5;
		assert value.getC() == "6";
	}
}

