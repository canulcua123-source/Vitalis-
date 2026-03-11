# 🔐 Sistema de Autenticación y Seguridad - Vitalis Backend

Este documento detalla la arquitectura de seguridad implementada en el proyecto **Vitalis Healthcare**, diseñada para cumplir con estándares de grado empresarial y manejo seguro de datos médicos.

---

## 🛠️ Tecnologías Utilizadas

- **Spring Security 6.x**: Framework de seguridad líder para aplicaciones Java/Spring.
- **JSON Web Tokens (JWT)**: Estándar para transmisión segura de información de identidad.
- **BCrypt**: Algoritmo de hashing robusto para el almacenamiento seguro de contraseñas.
- **Bucket4j**: Librería para control de tráfico y mitigación de ataques de fuerza bruta (Rate Limiting).

---

## 🚀 Características de Seguridad

### 1. Autenticación Stateless (JWT)
El sistema no almacena sesiones en el servidor, lo que permite una escalabilidad horizontal infinita.
- **Access Token**: Token de acceso firmado digitalmente que contiene la identidad y roles del usuario.
- **Seguridad Criptográfica**: Todos los tokens se firman con una clave secreta configurable vía variables de entorno (`JWT_SECRET_KEY`).

### 2. Control de Acceso por Roles (RBAC)
Vitalis implementa una jerarquía estricta de permisos gestionada por Spring Security:

| Rol | Descripción |
| :--- | :--- |
| **ROLE_PATIENT** | Acceso a agendamiento, historial propio y pagos. |
| **ROLE_DOCTOR** | Acceso a agenda médica diaria y gestión de disponibilidad. |
| **ROLE_ADMIN** | Control total, incluyendo el registro de nuevo personal médico. |

### 3. Filtros Personalizados
La seguridad se inyecta en el pipeline de peticiones mediante filtros especializados:
- **JwtAuthenticationFilter**: Valida el token en cada petición antes de llegar al controlador.
- **RateLimitFilter**: Monitorea el flujo de peticiones por IP para evitar ataques DoS o scraping de datos médicos.

---

## 🛠️ Configuración Técnica

### JwtService.java (Extraer Claims)
El servicio de JWT está configurado para incluir información clave en el payload del token, permitiendo al frontend identificar al usuario sin peticiones extra:
```java
public String generateToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, jwtExpiration);
}
```

### SecurityConfig.java (Chain de Seguridad)
Configuración de rutas y políticas:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(req -> req
            .requestMatchers("/api/v1/auth/**").permitAll() // Endpoints Públicos
            .requestMatchers("/api/v1/doctors/**").hasAnyRole("ADMIN", "PATIENT", "DOCTOR")
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));
    return http.build();
}
```

---

## 🛡️ Mejores Prácticas Implementadas

1. **Hashing de Contraseñas**: Nunca almacenamos texto plano. Usamos `BCryptPasswordEncoder` con un factor de costo balanceado.
2. **CORS Configurado**: Solo permitimos orígenes autorizados especificados en `application.yml`.
3. **Manejo de Excepciones**: Los errores de autenticación devuelven un JSON estructurado (`401 Unauthorized`) en lugar de páginas de error por defecto.
4. **Validación de Metadata en Pagos**: Los Webhooks de Stripe requieren una validación de firma criptográfica para evitar el "Spoofing" de pagos.

---

**Desarrollado por:** Jesus Guadalupe Canul Cua - Vitalis Project 🚀
