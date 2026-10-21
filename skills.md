# Directrices de Accesibilidad y Calidad Visual (WCAG AA / AAA)

Para todo desarrollo en **Lima en Acción** (Jetpack Compose y Layouts en Android):

## 1. Contraste de Texto y Color (WCAG AA & AAA)
- **Texto Normal (< 18pt o < 14pt bold):** Ratio de contraste mínimo de **4.5:1** (WCAG AA) contra el fondo. En pantallas críticas o de emergencia, priorizar **WCAG AAA (> 7:1)** con tonos oscuros de alta densidad (`#111827` o `#1F2937`) sobre fondo blanco.
- **Texto Grande (≥ 18pt o ≥ 14pt bold):** Ratio de contraste mínimo de **3.0:1**.
- **Prohibición de Colores Lavados:** No utilizar colores pasteles claros, lavados o naranjas desvanecidos para textos numéricos sobre fondos claros.
- **Modo Daltónico (Deuteranopía / Protanopía):**
  - Utilizar el espectro **Azul Intenso** (`#0D47A1` o `#005691`) para botones y componentes de acción primaria, dado que el azul es universalmente distinguible para personas con deficiencia al rojo y verde.
  - El icono o texto interior de los botones de llamada debe ser blanco puro (`#FFFFFF`), logrando un contraste superior a **9:1** (WCAG AAA).
  - Nunca transmitir información únicamente por medio del color; incorporar siempre texto, etiquetas o iconos descriptivos complementarios (WCAG 1.4.1).

## 2. Área Táctil Accesible (Touch Target)
- Todo elemento accionable (botones, chips, iconos interactivos) debe tener un tamaño mínimo de **48dp x 48dp** (WCAG 2.5.5) para evitar fallos de pulsación durante emergencias o temblores motores.

## 3. Soporte para Lectores de Pantalla (TalkBack)
- Todo icono y componente interactivo debe contar con un `contentDescription` descriptivo y contextualizado (ej. `"Llamar a SAMU al número 106"` en lugar de simplemente `"Llamar"`).

## 4. Tipografía e Internacionalización Dinámica
- Toda tipografía debe escalar proporcionalmente utilizando la función generadora `getTypography(scale)` sin dejar tamaños rígidos en `labelMedium` o `labelSmall`.
- Todo texto visible debe obtenerse del gestor de localización (`LocalAppStrings`) para reaccionar al instante al cambio de idioma (ES / EN).
