---
name: HearBoost
colors:
  surface: '#0a1422'
  surface-dim: '#0a1422'
  surface-bright: '#303a49'
  surface-container-lowest: '#050e1c'
  surface-container-low: '#121c2a'
  surface-container: '#16202f'
  surface-container-high: '#212a39'
  surface-container-highest: '#2c3545'
  on-surface: '#d9e3f7'
  on-surface-variant: '#bacac3'
  inverse-surface: '#d9e3f7'
  inverse-on-surface: '#273140'
  outline: '#85948e'
  outline-variant: '#3c4a45'
  surface-tint: '#38debb'
  primary: '#44e5c2'
  on-primary: '#00382d'
  primary-container: '#00c9a7'
  on-primary-container: '#004e40'
  inverse-primary: '#006b58'
  secondary: '#ffb955'
  on-secondary: '#452b00'
  secondary-container: '#dc9100'
  on-secondary-container: '#4f3100'
  tertiary: '#57e886'
  on-tertiary: '#003918'
  tertiary-container: '#34cb6d'
  on-tertiary-container: '#005024'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#5ffbd6'
  primary-fixed-dim: '#38debb'
  on-primary-fixed: '#002019'
  on-primary-fixed-variant: '#005142'
  secondary-fixed: '#ffddb4'
  secondary-fixed-dim: '#ffb955'
  on-secondary-fixed: '#291800'
  on-secondary-fixed-variant: '#633f00'
  tertiary-fixed: '#6ffe99'
  tertiary-fixed-dim: '#4fe080'
  on-tertiary-fixed: '#00210b'
  on-tertiary-fixed-variant: '#005226'
  background: '#0a1422'
  on-background: '#d9e3f7'
  surface-variant: '#2c3545'
typography:
  display-lg:
    fontFamily: Poppins
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Poppins
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  headline-md:
    fontFamily: Poppins
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Roboto
    fontSize: 20px
    fontWeight: '400'
    lineHeight: 30px
  body-md:
    fontFamily: Roboto
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  label-lg:
    fontFamily: Roboto
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0.01em
  button:
    fontFamily: Poppins
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0.02em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 12px
  md: 24px
  lg: 40px
  touch-target: 56px
  margin-mobile: 24px
  gutter: 16px
---

## Brand & Style

This design system is engineered for the silver generation, focusing on radical clarity and cognitive ease. The personality is "Quietly Empowering"—it avoids the clinical sterility of medical software in favor of a sophisticated, deep-sea aesthetic that feels premium and calming.

The design style utilizes **Modern Corporate** principles with **Tonal Layering**. By using a "Deep Ocean" dark mode as the default, we reduce eye strain and light sensitivity often associated with aging eyes. The interface relies on high-contrast accents and generous scale to ensure that every interaction is intentional and successful. The emotional response should be one of immediate relief and regained control.

## Colors

The palette is anchored in a dark, immersive "Deep Ocean" foundation to maximize the visibility of active UI elements.

- **Primary (HearBoost Teal):** Used for essential actions and active states. It provides high contrast against the dark background.
- **Secondary (Warm Amber):** Specifically reserved for volume management and sound intensity indicators, providing a distinct visual "heat" map for sensory input.
- **Surface Hierarchy:** 
  - **Background (#07111F):** The deepest layer for the overall screen.
  - **Surface (#0F2035):** Used for primary content containers.
  - **Elevated (#1A3352):** Used for interactive elements or cards that require user attention.
- **Status Colors:** Success Green and Alert Red are used sparingly to signal connectivity and battery warnings.

## Typography

Typography prioritizes legibility above all else. **Poppins** is used for display and headings to provide a friendly, modern character. **Roboto** is utilized for body text and labels due to its exceptional readability and neutral performance at large scales.

**Accessibility Rules:**
- The minimum font size is set to 16px (equivalent to 16sp) to accommodate users with presbyopia.
- Avoid using light font weights; use Regular (400) for body and Semi-Bold (600+) for headings.
- Maintain generous line-height (1.5x) to prevent "crowding" of text lines.

## Layout & Spacing

The design system employs a strict **8dp grid** to ensure logical alignment and predictable spacing.

**Layout Philosophy:**
- **Fluid Grid:** Content expands to fill the screen width with a consistent 24dp margin on either side.
- **Large Touch Targets:** Every interactive element has a minimum hit area of 56x56dp. This accounts for reduced motor dexterity.
- **Vertical Rhythm:** Use 24dp (md) spacing between distinct sections and 12dp (sm) within content groups.
- **Primary Actions:** The most critical action (e.g., "Connect Device" or "Mute") must always reside in the top 50% of the screen (above the fold).

## Elevation & Depth

To avoid the confusion of complex shadows, this design system uses **Tonal Layers** and **Stroke Definition**.

- **Depth through Color:** Elements are elevated by moving from the base #07111F to #1A3352.
- **Active States:** Interactive cards should use a 2px inner-border of #00C9A7 (Teal) when active or selected, rather than a shadow.
- **Separation:** Use subtle 1px dividers in #1A3352 to separate list items without adding visual clutter. 
- **No Blurs:** Avoid glassmorphism or background blurs as they can cause visual distortion for users with cataracts or high-order aberrations.

## Shapes

The shape language is **Pill-shaped (3)**. This approach eliminates "sharpness" and creates a friendly, tactile interface that feels safe and modern.

- **Buttons:** Always use full-round pill shapes.
- **Cards:** Use 24dp (rounded-lg) for container corners to maintain a soft look.
- **Input Fields:** Use 16px (rounded-md) to distinguish them clearly from buttons.

## Components

### Buttons
- **Primary Button:** Pill-shaped, #00C9A7 background with dark text. 56dp height.
- **Volume Slider:** Thick track (12dp) in #0F2035 with a large, Warm Amber (#F5A623) thumb (40dp diameter) for easy grabbing.

### Cards & Lists
- **Service Cards:** Use the Elevated Surface (#1A3352). All cards are tappable and should span the full width minus margins.
- **List Items:** Minimum height of 72dp to ensure the text and icon are easily legible and reachable.

### Inputs & Selection
- **Toggle Switches:** Oversized to ensure the "On/Off" state is unmistakable. Use Teal for 'On' and a neutral grey for 'Off'.
- **Checkboxes:** Minimum 32x32dp visual size within the 56dp hit area.

### Specialized Components
- **Hearing Profile Visualizer:** A simplified bar chart using Teal and Amber to show frequency boosting, designed with heavy lines (4dp+) for clarity.
- **Quick-Access Battery Pill:** A persistent, high-contrast indicator in the header showing the hearing aid's charge level.