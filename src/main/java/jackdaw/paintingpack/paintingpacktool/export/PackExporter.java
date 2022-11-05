package jackdaw.paintingpack.paintingpacktool.export;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jackdaw.paintingpack.paintingpacktool.mav.Controller;
import jackdaw.paintingpack.paintingpacktool.util.PaintingEntry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackExporter {

    final static Gson gson = new Gson();
    final List<PaintingEntry> paintings;
    final Controller controller;

    public PackExporter(Controller controller, List<PaintingEntry> paintings) {
        this.paintings = paintings;
        this.controller = controller;
    }

    public void export(File zipFile) {

        var modId = controller.getModId("");
        String inZipPath = String.format("assets/%s/textures/painting/", modId.isEmpty() ? "paintings" : modId);

        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
             Writer writer = new OutputStreamWriter(zipStream)) {
            //generate subfolders
            zipStream.putNextEntry(new ZipEntry(inZipPath));

            //copy over images
            for (PaintingEntry entry : paintings) {
                zipStream.putNextEntry(new ZipEntry(inZipPath + entry.fileName()));

                if (!entry.renamed().isBlank()) {
                    String dest = entry.absoluteImagePath().replace(entry.fileName(), entry.renamed());
                    Files.copy(Files.copy(Path.of(entry.absoluteImagePath()), Path.of(dest)), zipStream);
                } else
                    Files.copy(Path.of(entry.absoluteImagePath()), zipStream);
            }
            //make painting json
            zipStream.putNextEntry(new ZipEntry("paintings++.json"));
            //prepare content for json
            JsonObject toFile = new JsonObject();
            toFile.add("paintings", paintingEntries());
            //write content to json
            gson.toJson(toFile, writer);
            writer.flush();

            //make pack mcmeta
            zipStream.putNextEntry(new ZipEntry("pack.mcmeta"));
            JsonObject packFile = new JsonObject();
            packFile.add("pack", mcMetaContent());
            gson.toJson(packFile, writer);
            writer.flush();

        } catch (IOException e) {
            System.out.println(e);
        }

    }

    private JsonArray paintingEntries() {
        JsonArray array = new JsonArray();
        for (PaintingEntry entry : paintings) {
            JsonObject el = new JsonObject();
            var name = entry.fileName().substring(0, entry.fileName().length() - 4);
            var uniqueName = controller.getModId(":") + name;
            el.addProperty("name", uniqueName);
            el.addProperty("x", entry.size().getKey());
            el.addProperty("y", entry.size().getValue());
            array.add(el);
        }
        return array;
    }

    private JsonObject mcMetaContent() {
        JsonObject content = new JsonObject();
        content.addProperty("pack_format", 9);
        content.addProperty("description", "Painting Pack made with Painting Pack Maker!");
        return content;
    }
}
