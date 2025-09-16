# HSU Expense App - Color Code Reference

## 📋 Overview
This document provides a comprehensive reference of all color codes used in the HSU Expense Android application. The app uses a Material Design color system with support for both light and dark themes.

## 🎨 Primary Color Palette

### Material Design Colors
| Color Name | Hex Code | RGB | Usage |
|------------|----------|-----|-------|
| **Purple 500** | `#FF6200EE` | `rgb(98, 0, 238)` | Primary brand color, App Bar |
| **Purple 700** | `#FF3700B3` | `rgb(55, 0, 179)` | Status bar, Primary variant |
| **Purple 200** | `#FFBB86FC` | `rgb(187, 134, 252)` | Secondary elements |
| **Teal 200** | `#FF03DAC5` | `rgb(3, 218, 197)` | Secondary brand color |
| **Teal 700** | `#FF018786` | `rgb(1, 135, 134)` | Secondary variant |

### Accent & Action Colors
| Color Name | Hex Code | RGB | Usage |
|------------|----------|-----|-------|
| **Accent Color** | `#FF4CAF50` | `rgb(76, 175, 80)` | Primary buttons, success states |
| **Edit Color** | `#FF2196F3` | `rgb(33, 150, 243)` | Edit actions, links |
| **Error Color** | `#FFF44336` | `rgb(244, 67, 54)` | Error states, delete actions |
| **Orange Accent** | `#FFFF9800` | `rgb(255, 152, 0)` | Average values, warnings |

## 🌙 Theme-Based Colors

### Light Theme Colors
| Color Name | Hex Code | RGB | Usage |
|------------|----------|-----|-------|
| **Background** | `#FFFFFFFF` | `rgb(255, 255, 255)` | Main app background |
| **Surface** | `#FFF5F5F5` | `rgb(245, 245, 245)` | Card surfaces, elevated elements |
| **Text Primary** | `#FF000000` | `rgb(0, 0, 0)` | Primary text, headlines |
| **Text Secondary** | `#FF555555` | `rgb(85, 85, 85)` | Secondary text, descriptions |
| **Card Background** | `#FFFFFFFF` | `rgb(255, 255, 255)` | Card content background |
| **Input Background** | `#FFF8F8F8` | `rgb(248, 248, 248)` | Text input fields |
| **Card Border** | `#FFE0E0E0` | `rgb(224, 224, 224)` | Subtle borders |
| **Input Border** | `#FFEEEEEE` | `rgb(238, 238, 238)` | Input field borders |

### Dark Theme Colors
| Color Name | Hex Code | RGB | Usage |
|------------|----------|-----|-------|
| **Background** | `#FF121212` | `rgb(18, 18, 18)` | Main app background |
| **Surface** | `#FF1E1E1E` | `rgb(30, 30, 30)` | Card surfaces, elevated elements |
| **Text Primary** | `#FFFFFFFF` | `rgb(255, 255, 255)` | Primary text, headlines |
| **Text Secondary** | `#FFCCCCCC` | `rgb(204, 204, 204)` | Secondary text, descriptions |
| **Card Background** | `#FF2D2D2D` | `rgb(45, 45, 45)` | Card content background |
| **Input Background** | `#FF383838` | `rgb(56, 56, 56)` | Text input fields |
| **Card Border** | `#FF404040` | `rgb(64, 64, 64)` | Subtle borders |
| **Input Border** | `#FF505050` | `rgb(80, 80, 80)` | Input field borders |

## 🎨 Special UI Elements

### Glassmorphism Effects
| Element | Color Code | Alpha | Usage |
|---------|------------|-------|-------|
| **Glass Card Background** | `#33FFFFFF` | 20% | Semi-transparent white overlay |
| **Glass Card Border** | `#66FFFFFF` | 40% | Subtle white border |
| **Glass Card Shadow** | `#1A000000` | 10% | Soft shadow effect |

### Button States
| Button Type | State | Color Code | Usage |
|-------------|-------|------------|-------|
| **Primary Button** | Normal | `#FF2196F3` | Default state |
| **Primary Button** | Pressed | `#FF1976D2` | Touch feedback |
| **Primary Button** | Disabled | `#FFCCCCCC` | Disabled state |
| **Accent Button** | Normal | `#FF4CAF50` | Success actions |
| **Cancel Button** | Normal | `#FF666666` | Cancel actions |

### Icon Colors
| Icon Type | Color Code | Usage |
|-----------|------------|-------|
| **Light Theme Icons** | `#FF666666` | Icons on light backgrounds |
| **Dark Theme Icons** | `#FFCCCCCC` | Icons on dark backgrounds |
| **Add Icon** | `@android:color/darker_gray` | FAB and add buttons |
| **System Icons** | `@android:color/darker_gray` | Vector drawable tint |

## 📊 Data Visualization Colors

### Currency & Amount Colors
| Usage | Color Code | RGB | Purpose |
|-------|------------|-----|---------|
| **Positive Amounts** | `#FF4CAF50` | `rgb(76, 175, 80)` | Income, positive values |
| **Negative Amounts** | `#FFF44336` | `rgb(244, 67, 54)` | Expenses, negative values |
| **Average Values** | `#FFFF9800` | `rgb(255, 152, 0)` | Average calculations |
| **Currency Symbols** | `#FF4CAF50` | `rgb(76, 175, 80)` | Currency indicators |

### Chart & Graph Colors
| Element | Color Code | Usage |
|---------|------------|-------|
| **Primary Data** | `#FF6200EE` | Main data series |
| **Secondary Data** | `#FF03DAC5` | Secondary data series |
| **Accent Data** | `#FF4CAF50` | Highlighted data |
| **Grid Lines** | `#FFE0E0E0` | Chart grid (light theme) |
| **Grid Lines** | `#FF404040` | Chart grid (dark theme) |

## 🎯 Selection & State Colors

### Card Selection States
| State | Color Code | Usage |
|-------|------------|-------|
| **Selected Card** | `#FF4CAF50` | Selected currency/language |
| **Default Card** | `#FFE0E0E0` | Unselected state |
| **Hover State** | `#FFF5F5F5` | Touch feedback |

### Tab Colors
| Element | Light Theme | Dark Theme | Usage |
|---------|-------------|------------|-------|
| **Selected Tab Text** | `@color/text_primary_color` | `@color/text_primary_color` | Active tab |
| **Unselected Tab Text** | `@color/text_secondary_color` | `@color/text_secondary_color` | Inactive tabs |
| **Tab Indicator** | `@color/text_primary_color` | `@color/text_primary_color` | Active tab underline |

## 🔧 Technical Implementation

### Color Reference System
```xml
<!-- Direct color references -->
<color name="colorPrimary">#FF4CAF50</color>

<!-- Theme attribute references -->
<item name="colorPrimary">@color/purple_500</item>
<item name="textPrimaryColor">@color/text_primary_color</item>

<!-- Dynamic theme switching -->
<item name="backgroundColor">@color/background_color</item>
<item name="cardBackgroundColor">@color/card_background_color</item>
```

### Theme Attribute Mapping
```xml
<!-- Custom theme attributes -->
<item name="backgroundColor">@color/background_color</item>
<item name="cardBackgroundColor">@color/card_background_color</item>
<item name="textPrimaryColor">@color/text_primary_color</item>
<item name="textSecondaryColor">@color/text_secondary_color</item>
<item name="accentColor">@color/accent_color</item>
<item name="editColor">@color/edit_color</item>
<item name="errorColor">@color/error_color</item>
```

## 📱 iOS SwiftUI Color Mapping

For iOS SwiftUI implementation, use these equivalent colors:

```swift
// Primary Colors
let primaryPurple = Color(red: 98/255, green: 0/255, blue: 238/255) // #6200EE
let accentGreen = Color(red: 76/255, green: 175/255, blue: 80/255)  // #4CAF50
let errorRed = Color(red: 244/255, green: 67/255, blue: 54/255)    // #F44336
let editBlue = Color(red: 33/255, green: 150/255, blue: 243/255)   // #2196F3
let averageOrange = Color(red: 255/255, green: 152/255, blue: 0/255) // #FF9800

// Glassmorphism
let glassBackground = Color.white.opacity(0.2)  // #33FFFFFF
let glassBorder = Color.white.opacity(0.4)      // #66FFFFFF
```

## 🎨 Color Usage Guidelines

### Consistency Rules
1. **Primary Actions**: Always use `accent_color` (#4CAF50) for primary buttons
2. **Destructive Actions**: Use `error_color` (#F44336) for delete/cancel actions
3. **Edit Actions**: Use `edit_color` (#2196F3) for edit/modify actions
4. **Amounts**: Green for positive, Red for negative, Orange for averages
5. **Text Hierarchy**: Primary text = black/white, Secondary text = gray variants

### Accessibility Considerations
- **Contrast Ratio**: All text combinations meet WCAG AA standards
- **Color Blindness**: Red/green combinations avoid pure red-green conflicts
- **Dark Mode**: Full dark theme support with appropriate contrast

### Maintenance Notes
- Colors are centralized in `colors.xml` for easy maintenance
- Theme attributes allow dynamic switching between light/dark modes
- Glassmorphism effects use alpha transparency for modern appearance
- All colors include full opacity (FF) prefix for Android compatibility

---

**Last Updated**: September 15, 2025
**App Version**: 1.0.0
**Design System**: Material Design 3