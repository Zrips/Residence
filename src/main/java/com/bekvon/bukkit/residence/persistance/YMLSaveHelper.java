package com.bekvon.bukkit.residence.persistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.ReaderException;

public class YMLSaveHelper {

    File f;
    Yaml yml = null;
    Map<String, Object> root;

    public YMLSaveHelper(File ymlfile) throws IOException {

        final YamlConstructor constructor = new YamlConstructor();
        final YamlRepresenter representer = new YamlRepresenter();
        representer.setDefaultFlowStyle(FlowStyle.BLOCK);

        final DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
        dumperOptions.setIndent(2);
        dumperOptions.setAllowUnicode(true);
        dumperOptions.setWidth(4096);

        try {
            Class.forName("org.yaml.snakeyaml.LoaderOptions");
            final LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
            loaderOptions.getClass().getMethod("setCodePointLimit", int.class).invoke(loaderOptions, Integer.MAX_VALUE);
            yml = new Yaml(constructor, representer, dumperOptions, loaderOptions);
        } catch (Throwable e) {
        }

        if (yml == null)
            yml = new Yaml(constructor, representer, dumperOptions);

        root = new LinkedHashMap<String, Object>();
        if (ymlfile == null)
            throw new IOException("YMLSaveHelper: null file...");
        f = ymlfile;
    }

    public void save() throws IOException {
        if (f.getParentFile() != null && !f.getParentFile().exists())
            f.getParentFile().mkdirs();
        if (f.isFile())
            f.delete();
        FileOutputStream fout = new FileOutputStream(f);
        OutputStreamWriter osw = new OutputStreamWriter(fout, StandardCharsets.UTF_8);
        yml.dump(root, osw);
        osw.close();
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        InputStream fis = new FileInputStream(f);
        try {
            root = (Map<String, Object>) yml.load(fis);
        } catch (ReaderException e) {
            System.out.println("[Residence] - Failed to load " + yml.getName() + " file!");
        }
        fis.close();
    }

    public Map<String, Object> getRoot() {
        return root;
    }
}
