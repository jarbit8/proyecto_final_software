# Pruebas de seguridad - OWASP ZAP

Escaneo *baseline* (pasivo) contra la aplicación desplegada. Las reglas y su severidad
se controlan en `rules.tsv` (`WARN` reporta sin romper el build, `FAIL` corta el pipeline).

## Ejecución manual

Con la aplicación corriendo en `http://localhost:8080`:

```bash
docker run --rm -v "$(pwd)/zap:/zap/wrk:rw" --network host zaproxy/zap-stable \
  zap-baseline.py -t http://localhost:8080 -c rules.tsv -r zap_report.html -I
```

El reporte HTML queda en `zap/zap_report.html`.

## Ejecución en Jenkins

La etapa **Pruebas de Seguridad** del `Jenkinsfile` levanta la aplicación con Docker,
corre el escaneo dentro de la misma red y archiva `zap_report.html` como artefacto.
