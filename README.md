


### Debug Mode

Two modes :
- none
- profiles  

In profile mode, if one of the specified profiles is active, the debug uai will be used. Since the esidoc api only expect a specific uai, it is recommanded to use this mode during dev/qualif.   
It should be only disabled in prod or when using custom uri (pointing to a json server for example) instead of the esidoc dev and qualif api.

### Commandes pour notice et license

- `mvn notice:check`
- `mvn notice:generate`
- `mvn license:check`
- `mvn license:format`
- `mvn license:remove`

### To run with external configuration

- `mvn spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--spring.config.additional-location=file:${PATH_PROPERTIES}/ESIDOC-WS/"`