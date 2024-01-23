- 넥서스 (http://172.20.1.29:8088)
    implementation 'vn.kisvn.language:kisv-language:0.1.2-20231222.084007-1'
- 넥서스 (http://172.71.11.47:8088/)
    implementation 'vn.kisvn.language:kisv-language:0.1.2-20240123.042142-1'

- configuration
  @Configuration
  @RequiredArgsConstructor
  public class LanguageConfiguration {
  		
  		// 환경별 다른 s3경로 데이터 사용
      private final Environment environment;
      
  		@Bean
      public LanguageService languageService(){
  
  				// 사용하는 namespace 리스트 생성.
          List<String> namespaceList =  Arrays.asList("common");
          
  				return new LanguageService(namespaceList , environment);
      }
  }

- 서비스 사용
  @RequiredArgsConstructor
  public class SomeService {
  
      private final LanguageService languageService;
  
      public void test(){
  				// namespace : "common", lanaguage : "en", key value : "Delete"
          String COMMON_DELETE_EN = languageService.getVal("common","en","Delete");
          Set<String> COMMON_KEYSET = languageService.getKeyset("common" , "en");
      }
  }
