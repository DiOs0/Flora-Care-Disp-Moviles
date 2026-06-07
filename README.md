# 🌱 FloraCare

**Tu asistente inteligente para el cuidado de plantas.**  
FloraCare es una aplicación Android nativa que te ayuda a gestionar tu jardín personal, descubrir nuevas especies, registrar plantas con fotos y personalizar tu experiencia de cuidado.

---

## 📋 Índice

- [Stack Tecnológico](#-stack-tecnológico)
- [Organigrama de Trabajo](#-organigrama-de-trabajo)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Instalación Rápida](#-instalación-rápida)
- [Contribuyentes](#-contribuyentes)

---

## 🛠 Stack Tecnológico

| Tecnología | Versión | Propósito |
|---|---|---|
| ![Kotlin](https://img.shields.io/badge/Kotlin-7F52B2?style=for-the-badge&logo=kotlin&logoColor=white) | 1.9+ | Lenguaje principal |
| ![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) | API 28–36 (Android 9–16) | SDK objetivo |
| ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) | 8.x + Kotlin DSL | Sistema de build |
| ![AndroidX](https://img.shields.io/badge/AndroidX-4285F4?style=for-the-badge&logo=android&logoColor=white) | Core KTX 1.18.0 | Librerías base |
| ![Material Design](https://img.shields.io/badge/Material%20Design-757575?style=for-the-badge&logo=material-design&logoColor=white) | 1.13.0 | UI components |
| **ViewBinding** | — | Binding seguro de vistas |
| **Navigation Component** | — | Navegación entre fragments |
| **CameraX** | — | Captura de fotos de plantas |
| ![Glide](https://img.shields.io/badge/Glide-18BFFF?style=for-the-badge&logo=glide&logoColor=white) | 4.16.0 | Carga de imágenes |

---

## 🧑‍💻 Organigrama de Trabajo

```mermaid
graph TD
    A["📱 FloraCare App"] --> B["🏠 Reyes — Mi Jardín"]
    A --> C["🔍 Osorio — Explorar"]
    A --> D["📷 Jhon — Añadir Planta"]
    A --> E["⚙️ Milan — Ajustes"]

    B --> B1["Home / Dashboard"]
    B --> B2["Lista de plantas"]
    B --> B3["Adapter MiJardin"]

    C --> C1["Catálogo de especies"]
    C --> C2["Plantas destacadas"]
    C --> C3["ExploreAdapter / FeaturedAdapter"]

    D --> D1["Formulario de registro"]
    D --> D2["CameraManager (CameraX)"]
    D --> D3["Pantalla Login"]

    E --> E1["Pantalla de configuración"]
    E --> E2["Perfil de usuario"]
    E --> E3["Temas / preferencias"]

    subgraph Core["🧩 Base"]
        F["MainActivity + BottomNavigation"]
        G["Fragmentos navegables"]
    end

    A --> Core
```

---

## 📁 Estructura del Proyecto

```
app/src/main/java/com/uce/floracare/
├── MainActivity.kt                          # Actividad principal con Bottom Navigation
├── activities/
│   └── AuxiliarFragment.kt                  # Fragmento auxiliar
│
├── Jhon_AddPlant/                           # 👤 Jhon
│   ├── AddPlantFragment.kt                  #   Formulario para añadir planta
│   ├── CameraManager.kt                     #   Gestión de cámara con CameraX
│   └── Login.kt                             #   Pantalla de inicio de sesión
│
├── Milan_Ajustes/                           # 👤 Milan
│   └── AjustesFragment.kt                   #   Configuración y perfil
│
├── Osorio_Explore/                          # 👤 Osorio
│   ├── ExploreFragment.kt                   #   Pantalla de exploración
│   ├── ExploreAdapter.kt                    #   Adaptador del catálogo
│   ├── FeaturedAdapter.kt                   #   Adaptador de destacados
│   └── Plant.kt                             #   Modelo de datos
│
└── Reyes_MiJardin/                          # 👤 Reyes
    ├── MiJardinFragment.kt                  #   Vista "Mi Jardín"
    ├── Plant.kt                             #   Modelo de planta
    └── PlantAdapter.kt                      #   Adaptador de lista de plantas
```

### Layouts principales

```
app/src/main/res/layout/
├── activity_main.xml                        # Contenedor principal con BottomNavigation
├── activity_login.xml                       # Login
├── activity_mi_jardin.xml
├── fragment_add_plant.xml
├── fragment_ajustes.xml
├── fragment_auxiliar.xml
├── fragment_explore.xml
├── item_catalog_plant.xml                   # Card del catálogo
├── item_featured_plant.xml                  # Card de destacados
└── item_plant_card.xml                      # Card de planta en Mi Jardín
```

---

## ⚡ Instalación Rápida

### Requisitos previos

- **Android Studio** Hedgehog (2023.1.1) o superior
- **JDK** 17+
- Dispositivo o emulador con **API 28** mínimo

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/FloraCare.git

# 2. Abrir el proyecto en Android Studio
#    (File → Open → seleccionar la carpeta FloraCare)

# 3. Sincronizar Gradle
#    (esperar a que Android Studio descargue las dependencias)

# 4. Ejecutar
#    ▶ Run (Shift+F10) sobre el módulo :app
```

> 💡 **Tip:** Si encuentras errores de versión, revisa `gradle/libs.versions.toml` y ajusta las versiones según tu entorno.

---

## 👥 Contribuyentes

| Miembro | Rol | Módulo |
|---|---|---|
| **Reyes** | 🏠 Mi Jardín | Home, lista de plantas, adapters |
| **Osorio** | 🔍 Explorar | Catálogo, destacados, modelo de datos |
| **Jhon** | 📷 Añadir Planta | Formulario, cámara (CameraX), login |
| **Milan** | ⚙️ Ajustes | Configuración, perfil de usuario |

---

> 📌 **FloraCare** — Proyecto académico · Universidad Central del Ecuador · 8vo Semestre · Dispositivos Móviles
