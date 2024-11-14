package vn.kisvn.language.application.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Getter
@Setter
@Slf4j
@ToString
public class LanguageData {

    public String namespace;

    public HashMap<Lang, HashMap<String,String>> values;

    private String basePath;
    @Builder
    public LanguageData(String namespace , String env) throws Exception{
        this.namespace = namespace;
        this.values = new HashMap<>();
        this.basePath = setBaseUrlByEnv(env);
        for (Lang lang : Lang.values()) {
            String jsonPath = String.format(this.basePath , namespace, lang.name());
            HashMap<String ,String> value = getValuesFromURI(jsonPath);
            values.put(lang, value);
        }
    }

    @Builder
    public LanguageData(String namespace) throws Exception{
        this.namespace = namespace;
        this.values = new HashMap<>();
        loadLanguageDataFromResources();
    }

    private void loadLanguageDataFromResources() throws IOException {
        for (Lang lang : Lang.values()) {
            String resourcePath = namespace + "_" + lang.name().toLowerCase() + ".json";
            String jsonContent = readJsonFileFromResources(resourcePath);
            if (jsonContent != null) {
                JSONObject jsonObject = new JSONObject(jsonContent);
                HashMap<String, String> valueMap = new HashMap<>();
                for (String key : jsonObject.keySet()) {
                    valueMap.put(key, jsonObject.getString(key));
                }
                values.put(lang, valueMap);
            } else {
                throw new IOException("Failed to load resource: " + resourcePath);
            }
        }
    }

    private String readJsonFileFromResources(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (resource.exists()) {
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } else {
            return null;  // or handle it another way that fits your application's needs
        }
    }


    private static String setBaseUrlByEnv(String env){
        String baseUrl = null;
        switch (env) {
            case "stg":
                baseUrl = "https://d3hlxnvsdtzh1z.cloudfront.net/configuration/language/%s/%s.json";
                break;
            case "prod":
                baseUrl = "https://d1qa0lkmpv9yfv.cloudfront.net/configuration/language/%s/%s.json";
                break;
            default:
                baseUrl = "https://dba1km1zd7k7k.cloudfront.net/configuration/language/%s/%s.json";
                break;
        }
        return baseUrl;
    }


    private static HashMap<String,String> getValuesFromURI(String jsonPath) throws Exception{
        HashMap<String, String> value = new HashMap<>();
        try {
            URL url = new URL(jsonPath);
            JSONObject jsonObject = getJson(url);
            for (String key : jsonObject.keySet()) {
                String val = jsonObject.optString(key, null);
                value.put(key, val);
            }
        } catch (Exception e) {
            log.info("Failed to read url " +  jsonPath + " : "  + e.getMessage());
            throw e;
        }
        return value;
    }


    public static JSONObject getJson(URL url) throws Exception{
        String json = IOUtils.toString(url, Charset.forName("UTF-8"));
        return new JSONObject(json);
    }
}
