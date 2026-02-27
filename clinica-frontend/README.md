# 🏥 Vitalis - Premium Medical App

![Flutter](https://img.shields.io/badge/Flutter-%2302569B.svg?style=for-the-badge&logo=Flutter&logoColor=white)
![Dart](https://img.shields.io/badge/dart-%230175C2.svg?style=for-the-badge&logo=dart&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)
![Clean Architecture](https://img.shields.io/badge/Architecture-Clean-blue?style=for-the-badge)

Vitalis is a high-performance, production-ready medical ecosystem designed to bridge the gap between healthcare professionals and patients. Built with a focus on **visual excellence**, **security**, and **enterprise-grade architecture**, Vitalis offers a premium mobile experience for managing medical appointments and health history.

---

## 📖 Proyecto: Visión Vitalis

El objetivo de Vitalis es transformar la interacción médico-paciente mediante una interfaz intuitiva y moderna. Resolviendo la complejidad de los sistemas tradicionales, la app ofrece un registro inteligente, gestión de perfiles avanzada y una experiencia de usuario (UX) fluida que prioriza la accesibilidad y la rapidez de respuesta.

---

## 🛠️ Tecnologías Utilizadas

- **Flutter (Null Safety)**: Framework de alto rendimiento para el desarrollo multiplataforma.
- **Dart**: Lenguaje robusto y tipado para aplicaciones críticas.
- **Bloc (State Management)**: Gestión de estado predecible y escalable para la lógica de negocio.
- **Provider**: Utilizado para la gestión eficiente de estados locales en formularios complejos.
- **flutter_map + OpenStreetMap**: Mapas interactivos sin dependencia de proveedores privativos.
- **image_picker & flutter_image_compress**: Selección y optimización de imágenes en el cliente (reducción de ancho de banda).
- **Dio (HTTP Client)**: Cliente avanzado con interceptores para manejo de JWT y errores.
- **Clean Architecture & SOLID**: Estructura de código desacoplada, testeable y mantenible.

---

## 🔐 Seguridad Implementada

La seguridad es el núcleo de Vitalis, garantizando la privacidad de los datos médicos sensibles:

*   **Autenticación**: Implementación de tokens **JWT (JSON Web Tokens)** con almacenamiento seguro en el dispositivo.
*   **Validación en Tiempo Real**: Motores de validación que sanitizan cada input antes de ser procesado por el backend.
*   **HTTPS & SSL Pinning**: Comunicación cifrada de extremo a extremo para prevenir ataques de intermediarios (MITM).
*   **Sanitización de Datos**: Protección activa contra inyecciones y datos maliciosos en formularios.
*   **Manejo de Errores Controlado**: Capa de abstracción que evita fugas de información técnica en los mensajes de error al usuario.

---

## ✨ Funcionalidades Clave

- 👨‍⚕️ **Doble Perfil Dinámico**: Flujos de registro personalizados para Médicos y Pacientes.
- 📸 **Gestión de Identidad**: Subida de foto de perfil con previsualización circular y compresión inteligente.
- 📍 **Selector Geográfico**: Mapa interactivo para ubicar consultorios con **Reverse Geocoding** automático.
- 🤖 **Validaciones Inteligentes**: Sistema reactivo que guía al usuario, minimizando errores de entrada.
- 🎨 **UI Premium 2026**: Estética médica profesional con sombras suaves, bordes refinados y micro-interacciones.
- 🔘 **Segmented Control Animado**: Selector de roles fluido y visualmente atractivo.

---

## 🏗️ Arquitectura del Proyecto

El proyecto sigue una estricta **Arquitectura Limpia (Clean Architecture)** dividida en tres capas fundamentales:

1.  **Capa de Presentación (Presentation)**: Widgets de UI y BLoCs para manejar la lógica de la vista.
2.  **Capa de Dominio (Domain)**: Entidades de negocio y casos de uso (Usecases) puros.
3.  **Capa de Datos (Data)**: Repositorios, Modelos y Data Sources que gestionan la persistencia y APIs externas.

**Ventajas**:
- **Escalabilidad**: Fácil adición de nuevas funcionalidades sin afectar el núcleo.
- **Inyección de Dependencias**: Acoplamiento débil entre módulos.
- **Testing**: Capacidad de realizar pruebas unitarias y de integración de forma aislada.

---

## 💎 Experiencia de Usuario (UX)

- **Micro-interacciones**: Feedback visual inmediato al interactuar con botones e inputs.
- **Transiciones Suaves**: Navegación fluida entre pantallas para reducir la carga cognitiva.
- **Estados UI**: Manejo explícito de estados: `Loading`, `Error`, `Success`, y `Empty`.
- **Responsive Design**: Adaptación perfecta a diferentes tamaños de pantalla y densidades de píxeles.

---

## 🚀 Escalabilidad y Futuro

Vitalis está diseñado para crecer:
- ✅ Preparado para **Backends REST** de alta disponibilidad (Spring Boot/Node.js).
- ✅ Integración nativa con **Supabase/Firebase**.
- 🔜 **Próximas Mejoras**:
    - 💬 Chat en tiempo real médico-paciente.
    - 📹 Videoconsultas integradas.
    - 📅 Agenda inteligente con notificaciones Push.
    - 💳 Integración con pasarelas de pago (Stripe/Mercado Pago).
    - 🌙 Soporte completo para Dark Mode.

---

## 📝 Licencia
Este proyecto ha sido desarrollado como parte de un portafolio profesional de alta calidad, demostrando competencias en desarrollo móvil moderno y arquitectura de software.

---
*Desarrollado con ❤️ para transformar la salud digital.*
