package vn.kisvn.language.application.service;

import vn.kisvn.language.application.domain.LanguageData;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.kisvn.language.application.domain.Lang;

import java.util.*;

@Service
public class LanguageService {

    private final String env;

    @Getter
    private List<String> namespaceList;

    @Getter
    public List<LanguageData> languageData = new ArrayList<>();


    @Autowired
    public LanguageService( List<String> namespaceList , Environment env) {
        this.namespaceList = namespaceList;
        String[] profile =  env.getActiveProfiles();
        this.env = profile.length > 0 ? profile[0] : "prod";
        this.namespaceList = ( namespaceList != null
                && namespaceList.size() > 0)?
                namespaceList : Arrays.asList("common");
        updateLanguageData();
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

    @Scheduled(cron = "0 0 */2 * * *")
    public void updateData() {
        updateLanguageData();
    }

    private void updateLanguageData(){
        this.languageData = new ArrayList<>();
        for(String namespace : this.namespaceList){
            LanguageData languageData = new LanguageData(namespace , this.env);
            this.languageData.add(languageData);
        }
    }
}
