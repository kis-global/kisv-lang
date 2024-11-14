package vn.kisvn.language.application.service;

import lombok.extern.slf4j.Slf4j;
import vn.kisvn.language.application.domain.LanguageData;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.kisvn.language.application.domain.Lang;

import java.util.*;

@Slf4j
@Service
public class LanguageService {

    private final String env;

    @Getter
    private List<String> namespaceList;

    @Getter
    public List<LanguageData> languageData = new ArrayList<>();

    private boolean isInitialized = false;

    @Autowired
    public LanguageService( List<String> namespaceList , Environment env) {
        this.namespaceList = namespaceList;
        String[] profile =  env.getActiveProfiles();
        this.env = profile.length > 0 ? profile[0] : "prod";
        this.namespaceList = ( namespaceList != null
                && namespaceList.size() > 0)?
                namespaceList : Arrays.asList("common");
        log.info("name space list : "  + this.namespaceList);
        initLanguageData();
    }

    public String getVal(String namespace, String lang , String key ){
        Optional<LanguageData> targetNamespace = this.languageData.stream()
                .filter(ld -> namespace.equals(ld.getNamespace()))
                .findFirst();
        if(targetNamespace.isPresent()){
            LanguageData data =  targetNamespace.get();
            return data.getValues().get(Lang.valueOf(lang)).get(key);
        }
        return null;
    }

    public LanguageData getLanguageData(String namespace){
        Optional<LanguageData> targetNamespace = this.languageData.stream()
                .filter(ld -> namespace.equals(ld.getNamespace()))
                .findFirst();
        if(targetNamespace.isPresent()){
            LanguageData data =  targetNamespace.get();
            return data;
        }
        return null;
    }

    public Set<String> getKeyset(String namespace, String lang) {
        Optional<LanguageData> targetNamespace = this.languageData.stream()
                .filter(ld -> namespace.equals(ld.getNamespace()))
                .findFirst();
        if (targetNamespace.isPresent()) {
            LanguageData data = targetNamespace.get();
            HashMap<String, String> langMap = data.getValues().get(Lang.valueOf(lang));
            if (langMap != null) {
                return langMap.keySet();
            }
        }
        return Collections.emptySet();
    }

//    public void testResource(String namespace)  {
//        log.info("test");
//        try {
//            LanguageData languageDataTest = new LanguageData(namespace);
//            HashMap<String, String> langMap = languageDataTest.getValues().get(Lang.valueOf("en"));
//            Set<String> output =  langMap.keySet();
//            for(String t : output){
//                log.info("t = " + t);
//            }
//        } catch (Exception e) {
//            log.error(" e : " + e);
//        }
//    }



    private void initLanguageData() {
        log.info("Initializing Language data");
        this.languageData = new ArrayList<>();
        for (String namespace : this.namespaceList) {
            try {
                LanguageData data = fetchLanguageDataWithRetries(namespace, this.env, 10);
                if (data == null) {
                    log.info("Failed to fetch data for namespace: " + namespace + " after 10 retries, attempting to load from local resources.");
                    data = new LanguageData(namespace); // Fallback to local resources
                    log.info("Success getting data from resources");
                }
                this.languageData.add(data);
            } catch (Exception e) {
                log.error("Error initializing language data for namespace: " + namespace, e);
            }
        }
        isInitialized = true;
    }

    private LanguageData fetchLanguageDataWithRetries(String namespace, String env, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                log.info("Attempt " + (attempt + 1) + " fetching for namespace : " + namespace  );
                LanguageData result = new LanguageData(namespace, env);
                log.info("Fetching " + namespace +  " Success !!!!!!!");
                return result;
            } catch (Exception e) {
                log.error("Attempt " + (attempt + 1) + " failed for namespace: " + namespace);
                attempt++;
            }
        }
        return null;
    }

    //    @Scheduled(cron = "0 */10 * * * *")

    @Scheduled(cron = "0 */10 * * * *")
    public void updateData() {
        if (!isInitialized) {
            log.info("Initialization not complete. Skipping update.");
            return; // Skip the update if initialization is not complete
        }
        log.info("Updating language data");
        for (String namespace : this.namespaceList) {
            log.info("-------- namespace : " + namespace +" -----------");
            try {
                LanguageData updatedLanguageData = fetchLanguageDataWithRetries(namespace, this.env, 10);
                if (updatedLanguageData != null) {
                    replaceLanguageDataForNamespace(namespace, updatedLanguageData);
                    log.info("Successfully updated language data for namespace: " + namespace);
                } else {
                    log.error("Failed to update language data for namespace: " + namespace + " after 10 retries.");
                }
            } catch (Exception e) {
                log.error("Failed to update language data for namespace: " + namespace, e);
            }
            log.info("-----------------------------------");
        }
    }

    private void replaceLanguageDataForNamespace(String namespace, LanguageData newData) {
        System.out.println("languageData1 : " + languageData);
        languageData.removeIf(ld -> ld.getNamespace().equals(namespace));  // Remove old data for the namespace
        languageData.add(newData);  // Add the updated data
        System.out.println("languageData2 : " + languageData);
    }
}
