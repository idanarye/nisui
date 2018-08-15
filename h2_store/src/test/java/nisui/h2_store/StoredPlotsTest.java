package nisui.h2_store;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.assertj.core.api.Assertions;
import org.junit.*;

import nisui.core.DynamicExperimentValueHandler;
import nisui.core.plotting.*;

public class StoredPlotsTest extends TestsBase {
    @Test
    public void writeStoredPlots() throws SQLException {
        H2ResultsStorage<?, ?> storage = new H2ResultsStorage<>(tmpDbFileName(), new DynamicExperimentValueHandler(), new DynamicExperimentValueHandler());
        storage.prepareStorage();

        ArrayList<PlotEntry> savedPlots = new ArrayList<>();
        savedPlots.add(PlotEntry.buildNew(
            "plot1",
            new PlotAxis("Time", ScaleType.LINEAR, "Seconds", "time"),
            new PlotFilter("Day", FilterType.TEXTUAL_SINGLE, "", "day"),
            new PlotFormula("Temperature", "T", ScaleType.LOGARITHMIC, "Celsius", "temp")));

        try (H2ResultsStorage<?, ?>.Connection con = storage.connect()) {
            try (H2SaveStoredPlot<?, ?> saver = con.saveStoredPlots()) {
                for (PlotEntry plot : savedPlots) {
                    saver.save(plot);
                }
            }
        }

        ArrayList<PlotEntry> loadedPlots = new ArrayList<>();

        try (H2ResultsStorage<?, ?>.Connection con = storage.connect()) {
            try (H2ReadStoredPlots<?, ?> reader = con.readStoredPlots()) {
                for (PlotEntry plot : reader) {
                    loadedPlots.add(plot);
                }
            }
        }

        Assertions.assertThat(savedPlots.size()).isEqualTo(loadedPlots.size());
        for (int i = 0; i < savedPlots.size(); ++i) {
            PlotEntry saved = savedPlots.get(i);
            PlotEntry loaded = loadedPlots.get(i);

            Assertions.assertThat(saved).isEqualToComparingOnlyGivenFields(loaded, "key", "name");

            ArrayList<Function<PlotEntry, List<?>>> listExtractors = new ArrayList<>();
            listExtractors.add(plotEntry -> plotEntry.getAxes());
            listExtractors.add(plotEntry -> plotEntry.getFilters());
            listExtractors.add(plotEntry -> plotEntry.getFormulas());

            for (Function<PlotEntry, List<?>> listExtractor : listExtractors) {
                List<?> lSaved = listExtractor.apply(saved);
                List<?> lLoaded = listExtractor.apply(loaded);
                Assertions.assertThat(lSaved.size()).isEqualTo(lLoaded.size());
                for (int j = 0; j < lSaved.size(); ++j) {
                    Assertions.assertThat(lSaved.get(j)).isEqualToComparingFieldByField(lLoaded.get(j));
                }
            }
        }
    }
}
