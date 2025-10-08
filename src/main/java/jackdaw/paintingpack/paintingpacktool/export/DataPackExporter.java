package jackdaw.paintingpack.paintingpacktool.export;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jackdaw.paintingpack.paintingpacktool.controller.Controller;
import jackdaw.paintingpack.paintingpacktool.util.McVersions;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DataPackExporter {

    final static Gson gson = new Gson();
    final List<PaintingEntry> paintings;
    final Controller controller;

    public DataPackExporter(Controller controller, List<PaintingEntry> paintings) {
        this.paintings = paintings;
        this.controller = controller;
    }

    public void export(File zipFile) {

        var modId = getModId("");
        String inZipPath = String.format("assets/%s/textures/painting/", modId);

        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
             Writer writer = new OutputStreamWriter(zipStream)) {
            //generate subfolders
            zipStream.putNextEntry(new ZipEntry(inZipPath));

            //copy over images
            for (PaintingEntry entry : paintings) {
                //create new file with correct name
                zipStream.putNextEntry(new ZipEntry(inZipPath + entry.name()));
                //copy the original to the correctly named file
                Files.copy(Path.of(entry.absoluteImagePath()), zipStream);

                //make painting data files
                zipStream.putNextEntry(new ZipEntry(String.format("data/%s/painting_variant/%s.json", getModId(""), paintingName(entry))));
                gson.toJson(paintingFiles(entry), writer);
                writer.flush();
            }

            //make tag json file
            zipStream.putNextEntry(new ZipEntry("data/minecraft/tags/painting_variant/placeable.json"));
            //prepare content for json
            JsonObject tagToFile = new JsonObject();
            tagToFile.add("values", tagEntries());
            //write content to json
            gson.toJson(tagToFile, writer);
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

    private JsonObject paintingFiles(PaintingEntry entry) {
        JsonObject el = new JsonObject();
        var uniqueName = getModId(String.format(":%s", paintingName(entry)));
        el.addProperty("asset_id", uniqueName);
        el.addProperty("author", getModId(""));
        el.addProperty("title", paintingName(entry));
        el.addProperty("width", entry.size().getKey()/16);
        el.addProperty("height", entry.size().getValue()/16);
        return el;
    }

    private JsonArray tagEntries() {
        JsonArray array = new JsonArray();
        for (PaintingEntry entry : paintings) {
            var uniqueName = getModId(String.format(":%s", paintingName(entry)));
            array.add(uniqueName);
        }
        return array;
    }

    private JsonObject mcMetaContent() {
        JsonObject content = new JsonObject();
        JsonObject arrayContent = new JsonObject();
        arrayContent.addProperty("min_inclusive", 16);
        arrayContent.addProperty("max_inclusive", 1048576);
        content.addProperty("pack_format", McVersions.getPackVersion(controller.mcVersion.getValue()));
        content.add("supported_formats", arrayContent);
        content.addProperty("description", "Painting Pack made with Painting Pack Maker!");
        return content;
    }

    public String getModId(String append) {
        var id = controller.uniqueID.getText();
        return (id.isEmpty() ? "paintings" : id) + append;
    }

    private String paintingName(PaintingEntry entry) {
        return entry.name().substring(0, entry.name().length() - 4);
    }
}
