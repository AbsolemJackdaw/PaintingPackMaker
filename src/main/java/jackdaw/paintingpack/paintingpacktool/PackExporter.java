package jackdaw.paintingpack.paintingpacktool;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackExporter {

    final static Gson gson = new Gson();
    final List<PaintingEntry> paintings;

    public PackExporter(List<PaintingEntry> paintings) {
        this.paintings = paintings;
    }

    public void export(File zipFile) {
        JsonArray array = new JsonArray();

        for (PaintingEntry entry : paintings) {
            JsonObject el = new JsonObject();
            var name = entry.fileName().substring(0, entry.fileName().length() - 4);
            var uniqueName = HelloController.getModid(":") + name;
            el.addProperty("name", uniqueName);
            el.addProperty("x", entry.size().getKey());
            el.addProperty("y", entry.size().getValue());
            array.add(el);
        }

        JsonObject toFile = new JsonObject();
        toFile.add("paintings", array);
        var modid = HelloController.getModid("");
        String inZipPath = String.format("assets/%s/textures/painting/", modid.isEmpty() ? "paintings" : modid);

        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
             Writer writer = new OutputStreamWriter(zipStream)) {

            zipStream.putNextEntry(new ZipEntry(inZipPath));

            for (PaintingEntry entry : paintings) {
                zipStream.putNextEntry(new ZipEntry(inZipPath + entry.fileName()));
                Files.copy(Path.of(entry.absoluteImagePath()), zipStream);
            }
            zipStream.putNextEntry(new ZipEntry("paintings++.json"));
            gson.toJson(toFile, writer);

        } catch (IOException e) {
            System.out.println(e);
        }

    }
}
