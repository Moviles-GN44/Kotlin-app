# Uniandes Food - Analytics Pipeline & BQ Dashboard
**ISIS-3510: Desarrollo de Aplicaciones Móviles • Sprint 2 • Grupo GN-44**

Microservicio de analítica y visualización en tiempo real para el procesamiento de telemetría y respuestas a las **Business Questions (BQs)** del sistema.

---

## 🏛️ Arquitectura del Pipeline (3 Tiers CAS)

```
[ Android App (Kotlin) ] ---\
                             +---> [ Cloud Firestore (telemetry_events) ] 
[ Flutter App (Dart)   ] ---/                   |
                                                v
                                    [ Microservicio Python (Flask) ]
                                                |
                               +----------------+----------------+
                               |                                 |
                               v                                 v
                     [ Live Web Dashboard ]           [ High-Res PNG Charts ]
                     (http://localhost:5000)          (plots/*.png)
```

1. **Storage Tier:** Ingesta continua en la colección `telemetry_events` de Cloud Firestore.
2. **Processing Tier:** Servicio backend en Python (`app.py`) que agrega métricas, calcula promedios y distribuciones estadísticas.
3. **Visualization Tier:** Dashboard web interactivo en tiempo real con actualización automática (polling cada 2s) y generador de gráficos en alta resolución (`export_charts.py`).

---

## 📊 Business Questions Soportadas

* **BQ6 (Samuel - Tipo 2):** *¿Cuánto tiempo en promedio gasta un estudiante configurando filtros de precio, distancia y restricciones dietarias antes de elegir restaurante?*
  * **Métricas:** Duración promedio de sesión (segundos), histograma de tiempos (`<5s`, `5-10s`, `10-20s`, `>20s`), edificios de referencia más consultados (`RGD`, `ML`, `Franco`, `SD`, `C`, `W`), y desglose de preferencias dietarias y presupuesto.
* **BQ4 (Karin - Tipo 2):** *¿Qué porcentaje de estudiantes revisa fotos del menú antes de decidir ir al restaurante vs navegación directa?*
  * **Métricas:** Tasa de inspección fotográfica (%), locales más inspeccionados y conteo de platos visualizados.
* **Telemetría de Sensor & QR:** Distribución de calificaciones (1 a 5 estrellas) y tasa de completitud de reseñas.

---

## 🚀 Cómo Ejecutar

### 1. Iniciar el Dashboard en Tiempo Real
```bash
cd analytics_pipeline
python app.py
```
Abrir en el navegador: **`http://localhost:5000`**

### 2. Exportar Gráficos para el Informe / Diapositivas
```bash
python export_charts.py
```
Los gráficos se guardarán automáticamente en la carpeta `plots/`:
* `plots/bq6_filter_analysis.png`
* `plots/bq4_menu_inspection.png`
* `plots/qr_reviews_analysis.png`
