# DESIGN.md — Fresh Launcher

Dirección: **minimalista, fresco y tecnológico**, inspirado en la identidad visual de Lemon Cash.

> Fresh Launcher es un proyecto independiente y no tiene relación con Lemon Cash ni con ninguna de las marcas
> citadas abajo. Se analizaron como referencia de diseño: no usamos sus logos, nombres ni tipografías propietarias.

## 1. Referencia principal: identidad visual de Lemon Cash

Fuentes: media kit oficial (lemon.me/media-kit), sitio lemon.me y la captura de la app.

### Colores oficiales del media kit
| Nombre      | Hex       | Uso en Lemon |
|-------------|-----------|--------------|
| Main Green  | `#00DF1A` | Fondos de héroe, degradés, chevrons del menú |
| Lime Yellow | `#CFFF2E` | Acento: pestaña activa, chips ("Crece 36,2%"), íconos sobre negro, botón de idioma |
| Black       | `#121212` | Texto, botones pill, botones circulares, FAB |
| Leaf Green  | `#003D1B` | Verde profundo para texto sobre lima o fondos oscuros |

Neutros observados: fondo de la app gris claro (~`#EEEEEE`), tarjetas blancas `#FFFFFF`, fondo de secciones del sitio hueso (~`#F3F2EE`).

### Tipografía
- **Geist** (Vercel, licencia OFL): texto de interfaz y cuerpo. También usan **Geist Mono**.
- **Lemon Citrus**: tipografía propia para titulares. Es grotesca muy pesada, con interletrado apretado y a veces todo en mayúsculas. Es propietaria, así que la reemplazamos por **Geist Black (900)** con tracking negativo.
- Los números grandes (saldos) van en peso bold, con los decimales de menor jerarquía.

### Formas y componentes
- Tarjetas blancas con radios grandes (~24 dp) sobre fondo gris claro.
- Botones pill negros con texto lima ("Download the app").
- Botones de acción circulares negros con ícono lima y etiqueta debajo.
- Pestañas con la activa resaltada en lima y la inactiva en blanco.
- Barra inferior en forma de pill flotante, más un botón circular negro separado (QR) con ícono lima.
- Chips pequeños en lima para destacar estados.
- Íconos de línea, simples y monocromos.

### Atmósfera
- Degradé difuso (mesh gradient) de verde intenso → lima → crema, con grano sutil.
- Tono cercano y directo, en español rioplatense con voseo.
- Mucho aire, alto contraste y pocos elementos por pantalla.

## 2. Referencias adicionales y qué tomamos de cada una

| Referencia | Qué se ve | Qué tomamos |
|---|---|---|
| guglieri.com/feed | Fondo blanco, titular enorme en dos tonos (palabras en negro y otras en gris claro), hora local en vivo en el encabezado, tarjetas negras con radios grandes | **Reloj y fecha en dos tonos**; la hora en vivo como elemento central |
| endurosat.com | Fondo casi negro, titulares en mayúsculas pesadas, etiquetas pequeñas en mayúsculas con color de acento, estética técnica | **Etiquetas mono en mayúsculas** para datos (batería, fecha) y modo oscuro técnico |
| orano.group | Oscuro, mayúsculas finas con mucho tracking, barra de acento amarilla, líneas de grilla sutiles | **Líneas finas de grilla** y acento de color como barra lateral |
| sweet-tape.com | Lima intenso con verde muy oscuro, mayúsculas lúdicas, marquesinas | Confirma la **combinación lima + verde profundo** y un toque lúdico en el movimiento |
| curvy.dk/beagle | Negro puro, acento menta en botón pill, indicador de pasos con puntos | **Indicador de páginas con puntos** y pill de acento único |

## 3. Sistema de diseño de Fresh Launcher

### Paleta base (`Theme.kt`)
| Token        | Hex       | Uso |
|--------------|-----------|-----|
| `FreshGreen` | `#00DF1A` | Base del fondo "Fresh" en claro, brillos verdes |
| `FreshLime`  | `#CFFF2E` | Único acento: pill "Apps" en oscuro, punto de favoritas, cursor |
| `Ink`        | `#121212` | Texto en claro, pill en claro |
| `Leaf`       | `#003D1B` | Texto sobre lima, minutos del reloj en claro |
| `Bone`       | `#F3F2EE` | Texto en oscuro, fondo del cajón en claro |

### Tokens de color (propuesta inicial)
| Token            | Claro     | Oscuro    |
|------------------|-----------|-----------|
| `background`     | `#F3F2EE` | `#121212` |
| `surface`        | `#FFFFFF` | `#1C1C1C` |
| `onBackground`   | `#121212` | `#F3F2EE` |
| `muted` (2º tono)| `#B8B8B4` | `#5A5A57` |
| `accent`         | `#CFFF2E` | `#CFFF2E` |
| `onAccent`       | `#003D1B` | `#003D1B` |
| `brandGreen`     | `#00DF1A` | `#00DF1A` |
| `outline`        | `#121212` al 8 % | `#F3F2EE` al 8 % |

Regla: el lima es el **único** acento y se usa con moderación (un elemento por pantalla). El verde intenso queda para el fondo con degradé opcional.

### Tipografía (Geist, empaquetada en la app)
| Rol        | Fuente     | Peso | Tamaño | Tracking |
|------------|------------|------|--------|----------|
| Reloj      | Geist      | 900  | 72 sp  | -4 %     |
| Título     | Geist      | 800  | 28 sp  | -2 %     |
| App (lista)| Geist      | 500  | 22 sp  | -1 %     |
| Cuerpo     | Geist      | 400  | 16 sp  | 0        |
| Etiqueta   | Geist Mono | 500  | 11 sp  | +8 %, MAYÚSCULAS |

### Forma y espacio
- Radios: 24 dp para tarjetas, pill (50 %) para botones y chips, círculo para acciones.
- Grilla de 8 dp; márgenes laterales de 24 dp.
- Íconos de línea de 1,5 dp, monocromos.

### Movimiento
- Spring suave para abrir el cajón de apps y para los gestos; nada de rebotes exagerados.
- Entradas cortas (150–250 ms) con fundido y desplazamiento leve.
- Respetar la opción del sistema de reducir animaciones.

### Implementado
- **Fondos:** hay 7 (Fresh, Cítrico, Menta, Cielo, Lila, Coral y Terracota) en `Wallpapers.kt`. Todos usan el mismo lenguaje (manchas radiales difusas con grano en Overlay), con distinto patrón y paleta, y cada uno tiene versión clara (base luminosa para el texto negro) y oscura (negro puro). El tono `muted` de cada uno tiñe los minutos del reloj. El fondo elegido se aplica como fondo del sistema en la pantalla de inicio y en la de bloqueo (`WallpaperApplier`), y la ventana del launcher es transparente (`windowShowWallpaper`): así Android usa su transición de "volver al inicio". Al cambiar el tema se regenera solo si el fondo puesto sigue siendo nuestro. Si el usuario elige uno en la configuración, se aplica siempre. El cambio automático cada N días (1 a 30) se revisa al volver al inicio, sin trabajo en segundo plano, y pasa al siguiente de la lista.
- **Llegada al inicio:** el contenido aparece gradualmente con un zoom de 1,05 a 1 (resorte con amortiguación 0,9 y rigidez 320), desde `ON_START`.
- **Inicio:** se muestra sobre el fondo elegido. En claro, el fondo "Fresh" usa como base `#00DF1A`; en oscuro son manchas tenues sobre negro puro, por las pantallas AMOLED. Arriba, el reloj Geist 900 en dos tonos (minutos `#8FB894` en oscuro y hoja al 55 % en claro). Debajo, la fecha y el clima en Geist Mono. Abajo, hasta 7 favoritas (Geist 600, 30 sp) y la pill "Apps".
- **Cajón:** sube siguiendo el dedo con un resorte (amortiguación 0,86, rigidez 420) y el inicio retrocede al 95 % con 30 % de opacidad. Tiene buscador pill que ignora tildes, índice alfabético en mono y un punto lima en las favoritas. Mantener presionada una app muestra las pills de acción.
- **Favoritas:** mantener presionada y arrastrar las reordena; mantener presionada y soltar muestra "Quitar de inicio".
- **Clima:** Open-Meteo con la ubicación aproximada redondeada a unos 1 km. Se refresca cada 30 minutos; se guarda en °C y se muestra en la unidad elegida en la configuración. La configuración acredita a Open-Meteo, como pide su licencia CC BY 4.0.
- **Configuración:** botón redondo de 56 dp al lado del buscador, con el ícono de tres deslizadores dibujado con el trazo de 1,6. Abre una pantalla que entra desde la derecha, con opciones excluyentes como pills (la elegida rellena) y un interruptor con pista negra o lima. Incluye el idioma (por app, Android 13+), la unidad de temperatura (el clima se guarda en °C y se convierte al mostrarlo) y el teclado automático al abrir el cajón.
- Se respetan "Quitar animaciones", el formato de 12 o 24 horas y el idioma del dispositivo (inglés por defecto y español en `values-es`).

## 4. Principios del launcher
1. **Minimalista:** pantalla de inicio con reloj, fecha y unas pocas apps favoritas en texto.
2. **Fresco:** fondos con degradé difuso, claros u oscuros, y un único acento lima.
3. **Tecnológico:** etiquetas mono, datos precisos y líneas finas.
4. **Liviano:** debe ir fluido en teléfonos de gama media (se probó en un Samsung Galaxy A56 con Android 16).
