# Project Name

This README provides guidance on setting up and using the KisVN Language Library in your project.

## Nexus Repositories
We have the library hosted on two Nexus repositories. You can use either of them as per your network accessibility and preference.

1. Nexus Repository - 172.20.1.29:
   implementation 'vn.kisvn.language:kisv-language:0.1.2-20231222.084007-1'

2. Nexus Repository - 172.71.11.47:
   implementation 'vn.kisvn.language:kisv-language:0.1.2-20240123.042142-1'

## Configuration
To configure the language service in your application, define a configuration class as shown below:
```
@Configuration
@RequiredArgsConstructor
public class LanguageConfiguration {
    private final Environment environment;

    @Bean
    public LanguageService languageService() {
        // Define the namespace list to be used.
        List<String> namespaceList = Arrays.asList("common");
        return new LanguageService(namespaceList, environment);
    }
}
```
## Using the Service
To use the LanguageService in your application, follow the example provided below:
```
@RequiredArgsConstructor
public class SomeService {
    private final LanguageService languageService;

    public void test() {
        // Retrieve a value using namespace "common", language "en", and key "Delete"
        String COMMON_DELETE_EN = languageService.getVal("common", "en", "Delete");
        
        // Get the set of keys for the specified namespace and language
        Set<String> COMMON_KEYSET = languageService.getKeyset("common", "en");
    }
}
```
Ensure that your application has access to the specified Nexus repositories and the environment is properly set up to use the `LanguageService` class effectively.
