# CSS Toolkit Reference

Read `next-slide.css` for the authoritative class list. These are the foundational classes:

## Layout

| Class | Purpose |
|-------|---------|
| `.slide` | Main slide container — prevents page scrolling, applies padding |
| `cols-2`, `cols-3`, `cols-4` | CSS Grid equal columns — **preferred** column system (2 nesting levels) |
| `cols-7-5`, `cols-5-7`, `cols-8-4`, `cols-4-8` | Asymmetric grid columns (ratio-based) |
| `cols` + `--cols` custom property | Custom grid escape hatch: `style="--cols: 3fr 6fr 3fr"` |
| `.col` | Grid column child within a `cols-*` container |
| `.row` | Horizontal flex container — legacy column system (3 nesting levels) |
| `.column` | Flex child within a `.row` — legacy |
| `.col-1` … `.col-12` | 12-column flex widths — legacy |
| `.center` | Flex center + column direction + text-align center |
| `.vcenter` | Vertical centering (flex column) |
| `.responsive-container` | Flex wrapper with CSS container queries |
| `.gutters-10` | 10px inter-column padding |

## Content & Typography

| Class | Purpose |
|-------|---------|
| `.scroller` | Scrollable area — max-height 80svh, overflow-y auto, bottom fade gradient |
| `.content-heavy` | Wrapper for content-dense slides (use with `.content-area` child) |
| `.title-slide` | Title/section slide styling — h1 renders at 6vw |
| `.subtitle` | 1.5em text, 0.8 opacity |
| `.small` | 0.7em text, 0.9 opacity |
| `.no-bullets` | Removes list markers (for icon-prefixed lists) |

## Images

| Class | Purpose |
|-------|---------|
| `.title-logo-row` | Row of logos with responsive scaling (max-height 12vw) |
| `.logo-separator` | Plus/separator icon between logos (4vw) |
| `.icon-gallery` | Flex-wrap gallery for icons/logos (20px gap, 160px height) |
| `.image-focus` | Single prominent centered image (max-height 60vh, hover scale) |
| `.gallery` | Auto-grid layout (repeat auto-fit, minmax 200px, 1fr) |

## Special

| Class | Purpose |
|-------|---------|
| `.social-links` | Auto-prefixes icons by URL domain (GitHub, Twitter, LinkedIn, etc.) |
| `.callout` | Base callout box (padding, border-radius, margin-bottom) |
| `.callout-blue`, `.callout-green`, `.callout-purple` | Colored callout variants (semi-transparent backgrounds) |

## Spacing Utilities

| Class | Purpose |
|-------|---------|
| `.mt-0` … `.mt-3` | Margin-top: 0 to 3rem (`!important`) |
| `.mb-0` … `.mb-3` | Margin-bottom: 0 to 3rem (`!important`) |
| `.mtn-1` … `.mtn-3` | Negative margin-top: -1rem to -3rem (`!important`) |
| `.mbn-1` … `.mbn-3` | Negative margin-bottom: -1rem to -3rem (`!important`) |

Use to tighten or expand spacing between elements, e.g., `<div class="title-logo-row mbn-2">`.

## Automatic Behaviors

The CSS handles these automatically — don't re-implement them:

- **Social link icons**: URLs in `.social-links` get domain-appropriate FontAwesome icons via `::before`
- **Scroll fade**: `.scroller` adds a bottom fade gradient automatically
- **Image constraining**: All `img` elements get `max-width: 100%; height: auto; object-fit: contain`
- **Responsive stacking**: At container width ≤ 600px, `.row` and `cols-*` columns stack vertically
- **Theme adaptation**: Colors use VS Code theme variables — slides work in light and dark themes
- **VS Code marker div hiding**: The `cols-*` system automatically hides empty `<div class="code-line">` markers injected by VS Code's preview

## Theme Variables

When creating custom CSS, use these for theme compatibility:

```css
var(--vscode-foreground)            /* Text color */
var(--vscode-textLink-foreground)   /* Link color */
var(--vscode-editor-background)     /* Background */
```
