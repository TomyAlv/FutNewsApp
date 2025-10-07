<p align="center">
  <img src="bannerESPN.svg" alt="Banner del proyecto" width="700">
</p>


# ESPNApp



## Descripción
ESPNApp es una aplicación Android que replica una portada deportiva con pestañas para noticias, resultados en vivo, búsqueda de equipos y accesos adicionales desde un menú inferior. La navegación entre destinos (Noticias, Resultados, Menú, Buscar y detalle de equipo) se resuelve mediante el componente de navegación y un `BottomNavigationView` configurado en la actividad principal, que además persiste la preferencia de modo oscuro para toda la app.【F:app/src/main/res/navigation/nav_graph.xml†L1-L51】【F:app/src/main/java/com.example.espnapp/MainActivity.kt†L17-L76】

La portada agrupa noticias recientes de las principales ligas de fútbol, filtrando por la fecha de publicación del día en la zona horaria del usuario objetivo. Los resultados combinan tableros de varias ligas en paralelo y normalizan la información de cada encuentro para mostrar el estado del partido. La búsqueda permite consultar equipos a través de múltiples ligas y deduplica las coincidencias antes de mostrarlas al usuario.【F:app/src/main/java/com.example.espnapp/ui/news/NewsViewModel.kt†L19-L48】【F:app/src/main/java/com.example.espnapp/ui/news/NewsRepository.kt†L14-L70】【F:app/src/main/java/com.example.espnapp/ui/results/ResultsViewModel.kt†L27-L86】【F:app/src/main/java/com.example.espnapp/ui/results/ResultsRepository.kt†L13-L58】【F:app/src/main/java/com.example.espnapp/ui/search/SearchViewModel.kt†L28-L93】

## API utilizada
La aplicación consume la API pública de ESPN disponible [Public ESPN API](https://github.com/pseudo-r/Public-ESPN-API), encapsulada mediante Retrofit y OkHttp con un interceptor de logging básico para facilitar la depuración de llamadas HTTP.【F:app/src/main/java/com.example.espnapp/network/EspnRetrofit.kt†L8-L26】


🎥 **Video Demo del proyecto**

[![Ver Video demo en Google Drive](https://img.shields.io/badge/▶️%20Ver%20demo%20en%20Drive-blue?style=for-the-badge)](https://drive.google.com/file/d/1zfx7yV-L3W88r_n3Wkn7Y9F4XFk8XmKA/view?usp=sharing)

## Endpoints y estructura de datos
A continuación se listan los recursos REST consultados y los modelos relevantes que representan la respuesta en la app:

| Endpoint | Descripción | Parámetros | Modelo clave |
| --- | --- | --- | --- |
| `GET /apis/site/v2/sports/soccer/{league}/news` | Obtiene las noticias de una liga de fútbol. | `league` (código de liga, p. ej. `eng.1`). | `NewsResponse` → `Article` (titular, descripción, fecha ISO, enlaces e imágenes).【F:app/src/main/java/com.example.espnapp/network/EspnApiService.kt†L14-L15】【F:app/src/main/java/com.example.espnapp/model/NewsModels.kt†L6-L32】 |
| `GET /apis/site/v2/sports/soccer/{league}/scoreboard` | Devuelve la grilla de partidos y estados para la fecha solicitada. | `league`, `dates` (formato `yyyyMMdd` en UTC). | `ScoreboardResponse` → `Event` → `Competition` → `Competitor` y `Team` (nombres, marcadores, logos y estado del encuentro).【F:app/src/main/java/com.example.espnapp/network/EspnApiService.kt†L19-L24】【F:app/src/main/java/com.example.espnapp/model/espn/ScoreboardModels.kt†L6-L53】 |
| `GET /apis/site/v2/sports/soccer/{league}/teams` | Lista los equipos de una liga, usado para autocompletar búsquedas. | `league`. | `TeamsResponse` → `SportX` → `LeagueX` → `TeamX` → `Team` (nombre corto, largo y siglas).【F:app/src/main/java/com.example.espnapp/network/EspnApiService.kt†L26-L28】【F:app/src/main/java/com.example.espnapp/network/EspnApiService.kt†L35-L40】 |
| `GET {relativeUrl}` | Permite probar rutas dinámicas de noticias hasta encontrar contenido disponible. | URL relativa dentro del dominio ESPN. | Reutiliza `NewsResponse`.【F:app/src/main/java/com.example.espnapp/network/EspnApiService.kt†L30-L32】 |

## Decisiones técnicas y arquitectura
- **Arquitectura MVVM por feature**: Cada sección (Noticias, Resultados, Búsqueda) expone su estado mediante `ViewModel` + `LiveData`, delegando el acceso a red en repositorios específicos para mantener las vistas desacopladas de la capa de datos.【F:app/src/main/java/com.example.espnapp/ui/news/NewsViewModel.kt†L12-L44】【F:app/src/main/java/com.example.espnapp/ui/results/ResultsViewModel.kt†L15-L86】【F:app/src/main/java/com.example.espnapp/ui/search/SearchViewModel.kt†L21-L93】【F:app/src/main/java/com.example.espnapp/ui/news/NewsRepository.kt†L11-L70】【F:app/src/main/java/com.example.espnapp/ui/results/ResultsRepository.kt†L10-L58】
- **Agregación y resiliencia de llamadas**: Los repositorios lanzan múltiples requests en paralelo (por liga) y combinan los resultados, controlando errores parciales para ofrecer datos siempre que sea posible. Esto permite mostrar una portada multi-liga sin bloquearse por fallas puntuales.【F:app/src/main/java/com.example.espnapp/ui/news/NewsRepository.kt†L14-L44】【F:app/src/main/java/com.example.espnapp/ui/results/ResultsRepository.kt†L34-L58】
- **Normalización de datos**: Se formatea la fecha y el estado de cada partido respecto a la zona horaria definida y se eliminan duplicados en noticias y equipos para ofrecer información consistente.【F:app/src/main/java/com.example.espnapp/ui/news/NewsViewModel.kt†L23-L44】【F:app/src/main/java/com.example.espnapp/ui/results/ResultsViewModel.kt†L51-L86】【F:app/src/main/java/com.example.espnapp/ui/search/SearchViewModel.kt†L44-L78】
- **Experiencia de usuario**: La actividad principal configura la navegación por pestañas y ofrece un interruptor de tema con persistencia en `SharedPreferences`, manteniendo la preferencia de modo claro/oscuro entre sesiones.【F:app/src/main/java/com.example.espnapp/MainActivity.kt†L30-L76】
