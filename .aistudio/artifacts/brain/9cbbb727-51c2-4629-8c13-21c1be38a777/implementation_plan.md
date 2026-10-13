# Material 3 Expressive Audio Overhaul

Transform OpenSCQ30's visual design into a modern, tactile audio gear experience with Material 3 Expressive styling, featuring segmented card clusters, high-contrast audio-focused color containers, expressive typography, and refined controls across all device connection and settings screens.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following directions were confirmed during interactive clarification and guide the entire expressive redesign:

- **Confirmed Aesthetic Direction**: Modern audio gear styling featuring rich container shapes, depth, and vibrant sound-themed accents (neon teal & cyber electric lime/amber highlights against deep studio graphite/slate surfaces).
- **Confirmed Scope**: All screens updated with cohesive Material 3 Expressive components (Device Selection, Device Settings, Category Navigation, Category Overrides, Equalizer, Quick Presets, and App Info).
- **Confirmed Layout Strategy**: Segmented expressive card clusters with distinct rounded corner hierarchies (asymmetric and pill-shaped corner containers, expressive grouped surfaces).
- **Recommended Theme Default**: Retain full Dynamic Color (Material You) support when available on Android 12+, while upgrading the fallback light and dark color schemes to an electric audio gear palette (deep obsidian slate `#121316`, energetic cyan-teal primary `#00E5BF` / `#006A58`, warm copper/amber tertiary `#FF9E44`).

---

## 1. Overview & Core Concept

### What It Does
Upgrades OpenSCQ30 from utilitarian Android forms into an expressive companion app tailored for premium Soundcore headphones and earbuds. It introduces tactile audio hardware visual cues, expressive M3 shapes, pill-grouped control clusters, and dynamic visual indicators for device status, battery, and equalization.

### Target Audience / Persona
Soundcore headphone/earbud users (such as Life Q30, Space Q45, Liberty series) who want an open-source, private, and high-fidelity control app that looks and feels like specialized professional studio gear.

### Key Value
- **Tactile Clarity**: Control groups (ANC/Transparency, Equalizer bands, custom button configurations) become immediately recognizable at a glance with distinct container shapes.
- **Visual Delight**: Replaces plain list rows with expressive card clusters, floating segmented panels, and polished audio equipment iconography.
- **Design System Consistency**: Fully aligns with Material Design 3 Expressive guidelines while preserving zero-latency responsiveness and existing Bluetooth protocol bindings.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Device Selection & Pairing**:
   - Zero-state / scan screen presents an expressive hero badge with pulsing wireless radar aesthetic.
   - Paired and discovered devices appear in segmented pill cards with hardware-accented model icons, battery telemetry pills, and prominent connection toggles.
2. **Device Dashboard & Category Navigation**:
   - Device header displays connected model avatar, active connection state, and quick-status chips.
   - Category navigation items (General, Equalizer, Controls, Quick Presets) transition from plain text rows to expressive floating clustered tiles with dual-tone icon containers.
3. **Soundcore Equalizer & Sound Modes**:
   - Equalizer frequency sliders are framed inside sculpted vertical slider channels with frequency labels and tactile draggable thumb nodes.
   - Preset chips use filled tonal pills with instant visual selection states.
4. **Settings & Customization**:
   - Toggles and switches are hosted inside grouped card containers with rounded cap boundaries (top rounded, middle joined, bottom rounded).

### Visual Identity & Theme
- **Aesthetic Direction**: Professional audio hardware meets Material 3 Expressive. High-contrast surfaces, sculpted pill containers, subtle inner borders, and rhythmic card layouts.
- **Color Palette & Mood**:
  - *Dark Theme (Studio Night)*:
    - Background & Base Surface: `#0E1117` / `#161B22`
    - Surface Container Low / High: `#1C222D` / `#252D3B`
    - Primary Accent: Neon Electric Cyan `#00E5BF` (On Primary: `#00382E`)
    - Secondary Accent: Deep Wave Indigo `#82B1FF` (On Secondary: `#0A2346`)
    - Tertiary Accent: Acoustic Amber `#FFB74D` (On Tertiary: `#482900`)
  - *Light Theme (Studio Day)*:
    - Background & Base Surface: `#F6F8FA`
    - Surface Containers: `#FFFFFF`, `#EDF1F7`, `#E1E6EE`
    - Primary Accent: Deep Studio Cyan `#006B58`
    - Secondary Accent: Crisp Electric Blue `#1E5ACB`
- **Typography & Hierarchy**:
  - Display & Headline: Heavy, confident geometric sans-serif weights for device names and sound modes.
  - Title & Body: Clean, high-legibility tracking with distinct secondary meta-text for MAC addresses, firmware, and frequency labels (`Hz` / `dB`).
- **Component Styling & Layout**:
  - Card clusters use corner radii of `28.dp` for hero cards, `16.dp` for nested action clusters, and `20.dp` for segmented group edges.
  - Interactive elements have minimum `48.dp` touch boundaries and dynamic ripple response.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Segmented Card Cluster Architecture for Settings
- **Chosen Approach**: Replace linear flat list rows in `SettingCategoryScreen` and `CategorySelectionScreen` with segmented card clusters where items within the same category group share visual bounding cards with distinct top/middle/bottom rounded corners.
- **Why**: Creates distinct visual grouping matching modern audio hardware interfaces, drastically reducing visual noise and making complex headphone settings intuitive.
- **Alternatives Considered**: Keeping flat lists with dividers (felt generic and outdated); using floating cards for every single setting (created excessive vertical spacing and cognitive clutter).

### Decision 2: Expressive Equalizer Surface & Slider Redesign
- **Chosen Approach**: Elevate the interactive equalizer in `Equalizer.kt` and `SoundcoreEqualizerScreen.kt` into a specialized audio console view featuring vertical dB meter lines, gradient band indicators, and pill-shaped dB value readouts.
- **Why**: Equalizer tuning is the central hero feature of Soundcore devices; making it visually tactile turns EQ tuning into an enjoyable, expressive experience.
- **Alternatives Considered**: Standard Android sliders without custom channel styling (lacked audio personality).

### Decision 3: Preserving Rust FFI & Device Service Bindings
- **Chosen Approach**: Keep the underlying MVVM state flow, Rust uniFFI bindings (`com.oppzippy.openscq30.lib`), and background Bluetooth services completely untouched, applying all modifications exclusively to Compose UI and theming layers.
- **Why**: Guarantees rock-solid stability and zero Bluetooth protocol regressions while delivering a dramatic visual upgrade.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                       OpenSCQ30 UI                          │
├─────────────────────────────────────────────────────────────┤
│ Theme Layer                                                 │
│  ├── Color.kt (Audio-gear expressive palettes)              │
│  ├── Shape.kt (Asymmetric pill & clustered corner tokens)   │
│  └── Theme.kt (M3 Dynamic + Expressive Color Schemes)       │
├─────────────────────────────────────────────────────────────┤
│ Screen Components & Expressive Clusters                     │
│  ├── DeviceSelectionScreen                                  │
│  │    └── DeviceCard.kt (Pill-accented hardware cards)      │
│  ├── CategorySelectionScreen                                │
│  │    └── NavItem.kt (Segmented expressive action tiles)    │
│  ├── SettingCategoryScreen                                  │
│  │    ├── LabeledSwitch.kt (Clustered toggle surfaces)      │
│  │    └── ModifiableSelect.kt (Expressive chips & menus)    │
│  └── SoundcoreEqualizerScreen                               │
│       └── Equalizer.kt (Tactile studio console EQ view)     │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow & Actions
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 ViewModels & Device Service                 │
│  ├── OpenSCQ30RootViewModel / DeviceSelectionViewModel      │
│  └── DeviceService (Rust Bindings & BLE Connection)         │
└─────────────────────────────────────────────────────────────┘
```

### Component & State Mapping
- `DeviceCardContainer`: Renders hardware model glyphs inside rounded icon capsules with active connection badges and battery indicators.
- `NavItem`: Renders as an expressive interactive tile with tonal elevation, leading glyph pill, and chevron indicators with smooth ripple responses.
- `LabeledSwitch` & `Labeled`: Enhanced into expressive settings cards with rounded group corners and integrated status captions.
- `Equalizer`: Enhanced with vertical track guides, frequency badges (e.g. 100Hz, 3.2kHz), and live dB level chips.

---

## Verification Plan

### Compilation & Static Analysis
1. Execute `compile_applet` to verify clean compilation across all modified Jetpack Compose files.
2. Confirm zero regressions in Rust FFI imports or Bluetooth service references.

### Visual & Interactive Review
1. Verify device list screen rendering with expressive cards and preview states.
2. Verify category selection and device settings layouts in both Light and Dark themes.
3. Validate equalizer controls and slider precision across custom and preset profiles.
