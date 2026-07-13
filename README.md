# FloraCare

**Tu asistente inteligente para el cuidado de plantas.**  
FloraCare es una aplicación Android nativa que te ayuda a gestionar tu jardín personal, descubrir nuevas especies, registrar plantas con fotos, recibir recordatorios de riego y personalizar tu experiencia de cuidado.

---

## Stack Tecnológico

| Tecnología | Versión | Propósito |
|---|---|---|
| Kotlin | 1.9+ | Lenguaje principal |
| Android | API 28–36 (Android 9–16) | SDK objetivo |
| Gradle + Kotlin DSL | 8.x | Sistema de build |
| AndroidX Core KTX | 1.18.0 | Librerías base |
| Material Design 3 | 1.13.0 | UI components |
| ViewBinding | — | Binding seguro de vistas |
| MVVM + Clean Architecture | — | Arquitectura |
| Firebase Auth | — | Autenticación de usuarios |
| Firestore | — | Base de datos en la nube |
| Firebase Storage | — | Almacenamiento de imágenes |
| Room | — | Base de datos local offline |
| Retrofit + OkHttp | — | Consumo de API externa (Perenual) |
| Glide | 4.16.0 | Carga de imágenes |
| CameraX | — | Captura de fotos de plantas |
| Cloudinary | — | Upload de imágenes a la nube |
| WorkManager / AlarmManager | — | Notificaciones y recordatorios de riego |

---

## Arquitectura

```
MVVM + Clean Architecture
┌─────────────────────────────────────────────────┐
│  UI Layer (Fragments + ViewModels)              │
│  ┌──────────────┐  ┌──────────────────────────┐ │
│  │  Fragments   │→ │      ViewModels          │ │
│  │  (XML +      │  │  (UiState, LiveData)     │ │
│  │   ViewBinding)│  │                          │ │
│  └──────────────┘  └──────────┬───────────────┘ │
├─────────────────────────────────┼───────────────┤
│  Domain Layer                   │               │
│  ┌──────────────────────────────┴────────────┐  │
│  │           Use Cases                       │  │
│  │  (Casos de uso por funcionalidad)         │  │
│  └──────────────────────────────┬────────────┘  │
├─────────────────────────────────┼───────────────┤
│  Data Layer                     │               │
│  ┌──────────────────────────────┴────────────┐  │
│  │  Repositories (PlantRepository,           │  │
│  │  TaskRepository, UserRepository,          │  │
│  │  ImageRepository)                         │  │
│  ├───────────────────────────────────────────┤  │
│  │  Remote (Firebase Firestore, Firebase     │  │
│  │  Storage, Perenual API, Cloudinary)       │  │
│  ├───────────────────────────────────────────┤  │
│  │  Local (Room DB: PlantDao, TaskDao)       │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

---

## Funcionalidades

### Mi Jardín
- Vista en cuadrícula (2 columnas) o lista horizontal con toggle
- Lista de plantas del usuario con estado de riego
- Badge de estado: Normal / Atención Requerida / Urgente
- Overlay de confirmación de riego con long-press
- Edición rápida de frecuencia de riego
- Tareas pendientes de riego con acción directa
- Filtro de búsqueda por nombre
- Estado vacío con ilustración y frase de Aristóteles
- Estadísticas del jardín

### Explorar
- Catálogo de especies desde la API de Perenual
- Plantas destacadas en carrusel horizontal
- Búsqueda y filtrado en tiempo real
- Vista de detalle y registro de nuevas plantas desde el catálogo
- Indicador de resultados de búsqueda

### Añadir Planta
- Formulario con datos de la planta (nombre, científico, tipo, etc.)
- Captura de foto con CameraX
- Selección de características (interior, tropical, medicinal, etc.)
- Subida de imagen a Cloudinary
- Registro en Firestore con datos de la API de Perenual

### Detalle de Planta
- Información general (tipo, descripción)
- Cuidados y ciclo (riego, luz, ciclo de vida, temperatura)
- Características visuales con iconos (Interior/Exterior, Tropical, Medicinal, Tóxico humanos, Tóxico mascotas, Resistente sequía)
- Edición y eliminación de planta
- Estado de riego con fechas

### Autenticación
- Registro e inicio de sesión con Firebase Auth
- Recuperación de contraseña
- Manejo de sesión persistente

### Notificaciones
- Recordatorios de riego programados con AlarmManager
- Notificaciones push con Firebase Cloud Messaging
- Notificación de bienvenida

### Ajustes
- Perfil de usuario
- Configuración de preferencias

---

## Estructura del Proyecto

```
app/src/main/java/com/uce/floracare/
│
├── application/                          # Capa de UI (Android Framework)
│   ├── activities/
│   │   ├── Login.kt                      #   Activity de login
│   │   ├── MainActivity.kt               #   Activity principal con Bottom Navigation
│   │   └── WelcomeActivity.kt            #   Pantalla de bienvenida
│   │
│   ├── fragments/
│   │   ├── AddPlantFragment.kt           #   Formulario de nueva planta
│   │   ├── AjustesFragment.kt            #   Configuración y perfil
│   │   ├── AuxiliarFragment.kt           #   Fragmento auxiliar
│   │   ├── DetallePlantaFragment.kt      #   Detalle de planta con iconos
│   │   ├── EditarPlantaFragment.kt       #   Edición de planta existente
│   │   ├── ExploreFragment.kt            #   Catálogo y descubrimiento
│   │   ├── LoginFragment.kt              #   Inicio de sesión
│   │   ├── MiJardinFragment.kt           #   Vista principal "Mi Jardín"
│   │   ├── RecoveryFragment.kt           #   Recuperación de contraseña
│   │   └── RegisterFragment.kt           #   Registro de usuario
│   │
│   ├── adapters/
│   │   ├── PlantAdapter.kt               #   Adaptador con vista grid/lista
│   │   └── reyes_milan_osorio/
│   │       └── TaskAdapter.kt            #   Adaptador de tareas pendientes
│   │
│   ├── viewholder/
│   │   └── PendingTaskViewHolder.kt      #   ViewHolder de tareas
│   │
│   └── viewmodels/
│       ├── AddPlantViewModel.kt          #   ViewModel de añadir planta
│       ├── AjustesViewModel.kt           #   ViewModel de ajustes
│       ├── ExploreViewModel.kt           #   ViewModel de explorar
│       ├── MiJardinViewModel.kt          #   ViewModel de Mi Jardín
│       ├── PlantDetailViewModel.kt       #   ViewModel de detalle
│       └── ViewModelFactory.kt           #   Factory con inyección manual
│
├── data/                                 # Capa de datos
│   ├── local/
│   │   ├── dao/
│   │   │   ├── PlantDao.kt               #   DAO de plantas (Room)
│   │   │   └── TaskDao.kt                #   DAO de tareas (Room)
│   │   ├── database/
│   │   │   └── FloraCareDatabase.kt      #   Base de datos Room
│   │   ├── dto/
│   │   │   ├── PlantModel.kt             #   Modelo local de planta
│   │   │   └── Usuario.kt                #   Modelo de usuario
│   │   └── entity/
│   │       ├── PlantEntity.kt            #   Entidad Room de planta
│   │       └── TaskEntity.kt             #   Entidad Room de tarea
│   │
│   ├── remote/
│   │   ├── dto/
│   │   │   └── PlantEntity.kt            #   DTO de Firestore + API
│   │   └── FloraCareMessagingService.kt  #   FCM push notifications
│   │
│   ├── notifications/
│   │   ├── NotificationHelpe.kt          #   Helper de notificaciones
│   │   └── WelcomeNotification.kt        #   Notificación de bienvenida
│   │
│   ├── receiver/
│   │   └── WateringAlarmReceiver.kt      #   BroadcastReceiver de riego
│   │
│   └── scheduler/
│       ├── AndroidWateringScheduler.kt   #   Programador de alarmas
│       └── WateringScheduler.kt          #   Interface de scheduler
│
├── domain/                               # Capa de dominio (reglas de negocio)
│   ├── model/
│   │   ├── EstadisticasJardin.kt         #   Estadísticas del jardín
│   │   ├── PlantTask.kt                  #   Tarea de planta
│   │   ├── TaskEntity.kt                 #   Modelo de tarea
│   │   ├── UserPlant.kt                  #   Planta de usuario
│   │   ├── UserProfile.kt                #   Perfil de usuario
│   │   └── WateringStatus.kt             #   Enum de estado de riego
│   │
│   └── usecase/                          # 24 casos de uso
│       ├── ActualizarPerfilUsuarioUseCase.kt
│       ├── ActualizarPlantaUsuarioUseCase.kt
│       ├── ActualizarRiegoFrecuenciaUseCase.kt
│       ├── ActualizarRiegoUseCase.kt
│       ├── CerrarSesionUseCase.kt
│       ├── CompletarTareaPendienteUseCase.kt
│       ├── EliminarPlantaUsuarioUseCase.kt
│       ├── GenerarTareasPendientesUseCase.kt
│       ├── GeneratePlantTasksUC.kt
│       ├── GetUserProfileUC.kt
│       ├── ObtenerCatalogoPlantasUseCase.kt
│       ├── ObtenerDetallePlantaUseCase.kt
│       ├── ObtenerEstadisticasJardinUseCase.kt
│       ├── ObtenerPerfilUsuarioUseCase.kt
│       ├── ObtenerPlantaPorIdUseCase.kt
│       ├── ObtenerPlantasJardinUseCase.kt
│       ├── ObtenerTareasPendientesUseCase.kt
│       ├── RegistrarPlantaEnJardinUseCase.kt
│       ├── SincronizarCatalogoUseCase.kt
│       ├── SubirImagenUseCase.kt
│       └── WaterPlantUseCase.kt
│
├── repositories/                         # Repositorios (fuentes de datos)
│   ├── connections/remote/
│   │   ├── api/
│   │   │   ├── PerenualApiService.kt     #   API de especies (Retrofit)
│   │   │   └── RetrofitClient.kt         #   Cliente Retrofit
│   │   ├── cloudinary/
│   │   │   └── CloudinaryService.kt      #   Servicio de imágenes cloud
│   │   └── firebase/
│   │       ├── AuthManager.kt            #   Autenticación Firebase
│   │       ├── FirestoreManager.kt       #   CRUD Firestore
│   │       └── StorageManager.kt         #   Almacenamiento Firebase
│   ├── ImageRepository.kt               #   Interface de imágenes
│   ├── ImageRepositoryImpl.kt           #   Implementación de imágenes
│   ├── PlantRepository.kt               #   Repositorio de plantas
│   ├── TaskRepository.kt                #   Repositorio de tareas
│   └── UserRepository.kt               #   Repositorio de usuarios
│
├── design/
│   └── FloraCareDesignSimulation.kt     #   Simulación de diseño
│
└── utils/
    ├── CameraManager.kt                  #   Gestión de cámara (CameraX)
    └── PlantConstants.kt                 #   Constantes de la app
```

### Layouts (21 archivos)

```
app/src/main/res/layout/
├── activity_login.xml
├── activity_main.xml                     # BottomNavigation + fragment container
├── activity_mi_jardin.xml                # Mi Jardín con toggle vista
├── activity_welcome.xml
├── dialog_delete_plant.xml               # Confirmación de eliminación
├── fragment_add_plant.xml
├── fragment_ajustes.xml
├── fragment_auxiliar.xml
├── fragment_detalle_planta.xml           # Detalle con chips de características
├── fragment_editar_planta.xml
├── fragment_explore.xml
├── fragment_login.xml
├── fragment_recovery.xml
├── fragment_register.xml
├── item_catalog_plant.xml                # Card de catálogo (explorar)
├── item_char_chip.xml                    # Chip icono+texto para características
├── item_featured_plant.xml               # Card horizontal destacada
├── item_pending_task.xml                 # Tarea pendiente de riego
├── item_plant_card.xml                   # Card vertical (grid Mi Jardín)
├── item_plant_card_horizontal.xml        # Card horizontal (lista Mi Jardín)
└── item_task_card.xml
```

### Drawables (50 recursos)

| Icono | Uso |
|---|---|
| `interior.png` / `exterior.png` | Tipo de planta |
| `tropical.png` | Planta tropical |
| `medicine_plant.png` | Planta medicinal |
| `sequia.png` | Resistente a sequía |
| `humans_bad.png` | Tóxica para humanos |
| `no_dog.png` | Tóxica para mascotas |
| `planta_noplanta.png` | Estado vacío del jardín |
| `ic_logo.xml` | Logo de la app |
| `circle_alert_red.xml` | Badge de estado urgente |
| `circle_success_green.xml` | Badge de regado |

---

## Modelo de Datos

### PlantEntity (Firestore + Remote DTO)

| Campo | Tipo | Descripción |
|---|---|---|
| `firestoreId` | String | ID en Firestore |
| `id` | Int | ID de Perenual API |
| `nombreComun` | String | Nombre común |
| `nombreCientifico` | String | Nombre científico |
| `imagen` | String | URL de la imagen |
| `tipo` | String | Tipo de planta |
| `descripcion` | String | Descripción |
| `cicloVida` | String | Ciclo de vida |
| `nivelCuidado` | String | Nivel de cuidado |
| `caracteristicas` | Caracteristicas | Indoor, tropical, medicinal, tóxico, etc. |
| `riego` | Riego | Frecuencia y valor |
| `luzSolar` | List\<String\> | Requerimientos de luz |
| `temperatura` | Temperatura | Rango de temperatura |
| `ultimoRiego` | Long | Timestamp último riego |
| `wateringFrequencyDays` | Int | Días entre riegos |

### WateringStatus (Enum)

- `NORMAL` — Riego al día
- `ATENCION_REQUERIDA` — Se acerca el día de riego
- `URGENTE` — Se pasó del día de riego

---

## Instalación Rápida

### Requisitos previos

- **Android Studio** Hedgehog (2023.1.1) o superior
- **JDK** 17+
- Dispositivo o emulador con **API 28** mínimo
- Conexión a Internet (Firebase, Perenual API)

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/FloraCare.git

# 2. Abrir el proyecto en Android Studio
#    (File → Open → seleccionar la carpeta FloraCare)

# 3. Configurar Firebase
#    - Crear proyecto en Firebase Console
#    - Agregar google-services.json a app/
#    - Habilitar Authentication (Email/Password)
#    - Habilitar Firestore y Storage

# 4. Sincronizar Gradle y ejecutar
#    ▶ Run (Shift+F10) sobre el módulo :app
```

> **Tip:** Si encuentras errores de versión, revisa `gradle/libs.versions.toml` y ajusta las versiones según tu entorno.

---

## Uso de la App

1. **Regístrate o inicia sesión** con tu correo electrónico
2. **Explora** el catálogo de plantas desde la API de Perenual
3. **Añade plantas** a tu jardín con foto y datos personalizados
4. **Gestiona tu jardín** con vista en cuadrícula o lista horizontal
5. **Recibe recordatorios** de riego según la frecuencia configurada
6. **Mantén el registro** de riego con la confirmación por long-press
7. **Edita o elimina** plantas desde la vista de detalle

---

## Contribuyentes

| Miembro | Módulo | Responsabilidad |
|---|---|---|
| **Reyes** | Mi Jardín | Home, lista de plantas, adaptador con toggle grid/lista, tareas pendientes, estado de riego |
| **Osorio** | Explorar | Catálogo de especies, plantas destacadas, búsqueda, adaptadores |
| **Jhon** | Añadir Planta | Formulario de registro, cámara (CameraX), autenticación (login, registro, recuperación) |
| **Milan** | Ajustes | Configuración, perfil de usuario, notificaciones, scheduler de riego |

---

> **FloraCare** — Proyecto académico · Universidad Central del Ecuador · 8vo Semestre · Dispositivos Móviles
