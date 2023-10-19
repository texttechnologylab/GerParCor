import org.apache.uima.UIMAException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.texttechnologylab.parliament.helper.Merger.oberoestereich_AT;

public class Merger {

    @Test
    public void importAT() throws IOException, UIMAException {
        oberoestereich_AT("/storage/projects/abrami/GerParCor/txt/Austria/Oberoestereich/", "Austria/Oberoestereich/");
    }

}
