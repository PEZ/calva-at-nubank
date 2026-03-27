---
name: next-slide-editor
description: '**EDITING SKILL** — Create, edit, and style presentation slides for the next-slide system (VS Code markdown preview + CSS + EDN config). USE FOR: creating new slides, editing existing slides, adding slides to a deck, styling slide layouts, extending presentation CSS, choosing slide patterns, fixing slide layout issues. DO NOT USE FOR: slide navigation/runtime (that is next_slide.cljs), audio/narration generation, general markdown editing unrelated to presentations. INVOKES: file system tools (read/write slides, CSS, slides.edn), search tools (discover workspace image paths and existing slide patterns).'
---

# Next-Slide Editor

You are a **CSS-creative slide editor**. Your instinct is markdown for content, CSS classes for styling and layout, and HTML only for layout structure that Markdown cannot handle. When you need a visual effect, reach for the stylesheet before reaching for inline styles.

## When to Use This Skill

- Creating or editing presentation slides (markdown files in `slides/`)
- Styling slide layouts or extending `next-slide.css`
- Adding slides to a deck (`slides.edn`)
- Fixing slide layout or viewport-fit issues
- Choosing the right slide pattern for content

## Core Principles

1. **Markdown-first**: Use markdown for all content. Only introduce HTML `<div>` wrappers for layout (columns, centering, slide containers).
2. **CSS over inline styles**: If a style pattern will be used more than once, create a class in `next-slide.css`. Inline styles are acceptable only for true one-offs.
3. **Viewport fit**: Every slide should fit the preview window. Use `.scroller` when content must overflow. Never let content silently overflow without a scroll container.
4. **Simplicity**: Fewer divs = better slide. Don't add columns unless content genuinely needs side-by-side presentation. Ask: "Can this be pure markdown with just a `.slide` wrapper?"

## Before You Start

Before creating or editing slides:

1. **Read `slides.edn`** — understand the deck structure and file path conventions
2. **Determine image paths** — list directories to find where images live, then compute the correct **relative path** from each slide's location to the image directory (see [Path Conventions](#path-conventions))
3. **Read `next-slide.css`** — know what classes already exist before creating new ones
4. **Read existing slides** — understand the patterns and conventions already in use

## Create a Slide (Step-by-Step)

1. **Read `slides.edn`** — find the right insertion position for the new slide
2. **List image directories** — compute `{image-path}` relative to the slide file location
3. **Read `next-slide.css`** — check for existing classes that fit the content
4. **Choose a pattern** — pick from the Slide Patterns section below
5. **Write the `.md` file** — use the pattern with real content, verify blank lines around markdown in divs
6. **Update `slides.edn`** — insert the slide path at the correct position
7. **Run the verification checklist** — images exist, classes exist, EDN is valid

## Architecture

| Component | File | Purpose |
|-----------|------|---------|
| Slide content | `slides/*.md` | Markdown files rendered by VS Code preview |
| Deck config | `slides.edn` | Ordered vector of slide paths (authoritative) |
| Styling | `next-slide.css` | Presentation CSS toolkit (extend as needed) |
| CSS injection | `.vscode/settings.json` | `"markdown.styles": ["next-slide.css"]` wires the CSS into VS Code's markdown preview |
| Notes | `*-notes.md` | Auto-discovered speaker notes |

Slides render in **VS Code's built-in markdown preview** with CSS injection via the `markdown.styles` setting. Markdown works inside HTML divs. FontAwesome 6 icons are available via CDN import in the CSS.

## CSS Toolkit Reference

See [references/css-toolkit.md](references/css-toolkit.md) for the full class reference (layout, typography, images, spacing, theme variables, automatic behaviors).

Read `next-slide.css` for the authoritative, always-current list.

## Slide Patterns

### Simple Slide — Pure Markdown

When content is just headings, text, lists, or images with no multi-column needs:

```html
<div class="slide">

# My Topic

- Key point one
- Key point two
- Key point three

![Diagram]({image-path}/diagram.png)

</div>
```

**Note**: Blank lines around markdown inside HTML divs are required for markdown rendering.

### Two-Column Layout

Uses the `cols-*` CSS Grid system — 2 levels of nesting:

```html
<div class="slide cols-2">

# Feature Overview

<div class="col">

- Left side content
- More points here

</div>
<div class="col">

- Right side content
- Additional points

</div>
</div>
```

The heading spans full width automatically. Only `.col` divs become columns.

### Asymmetric Layout — Content + Image

```html
<div class="slide cols-7-5">

# How It Works

<div class="col">

- Step one explanation
- Step two explanation
- Step three explanation

</div>
<div class="col center">

![Feature]({image-path}/feature.png)

</div>
</div>
```

Available ratios: `cols-7-5`, `cols-5-7`, `cols-8-4`, `cols-4-8`.

### Custom Column Ratios

For non-standard splits, use the `--cols` custom property:

```html
<div class="slide cols" style="--cols: 3fr 6fr 3fr">

# Custom Layout

<div class="col">Narrow</div>
<div class="col">Wide</div>
<div class="col">Narrow</div>

</div>
```

### Title Slide

For opening or section divider slides:

```html
<div class="slide title-slide center">

# Big Title Here

A subtitle or tagline

</div>
```

### Title Slide with Logo Row

```html
<div class="slide title-slide center">

# Welcome

<div class="title-logo-row">
  <img src="{image-path}/logo-a.png" alt="Logo A" />
  <i class="fas fa-plus logo-separator"></i>
  <img src="{image-path}/logo-b.png" alt="Logo B" />
</div>

</div>
```

### Legacy Columns (`.row`/`.column`/`.col-N`)

Existing slides may use the older flexbox system (3 nesting levels, `.col-1` to `.col-12` widths). **Prefer `cols-*` for new slides.** When reading legacy slides, don't convert unless asked.

### Icon-Prefixed Lists

Use FontAwesome icons before list items with `.no-bullets` or let CSS handle the styling:

```markdown
* <i class="fas fa-rocket"></i> **Fast startup**
* <i class="fas fa-key"></i> **Full API access**
* <i class="fas fa-bolt"></i> **Live coding**
```

Common icons: `fa-key`, `fa-rocket`, `fa-bolt`, `fa-users`, `fa-terminal`, `fa-heart`, `fa-plus`, `fa-paint-brush`, `fa-plug`, `fa-code`

### Scrollable Content

For reference slides or content that exceeds viewport height:

```html
<div class="slide">

# API Reference

  <div class="scroller">

- Item 1: Description
- Item 2: Description
- …many items…
- Item 20: Description

  </div>
</div>
```

### Callout / Highlight Box

For emphasized content sections (classes exist in `next-slide.css`):

```html
<div class="slide">

# Key Insight

<div class="callout callout-blue">

**The important takeaway**: explained clearly here.

</div>
</div>
```

Available variants: `.callout-blue`, `.callout-green`, `.callout-purple`.

## Extending the CSS

Creating CSS classes is **part of slide authoring**, not a separate concern. When the existing toolkit doesn't cover your design need, extend `next-slide.css`.

### When to Create a New Class

- A styling pattern appears on more than one slide
- A slide needs a distinct visual treatment (comparison boxes, quote blocks, accent borders)
- You find yourself writing more than 2–3 inline style properties on an element

### CSS Design Guidelines

- **Relative units**: Use `em`, `rem`, `vw`, `vh` for responsive sizing
- **Theme variables**: Use `var(--vscode-foreground)` etc. for theme-compatible colors
- **Semi-transparent backgrounds**: `rgba(R, G, B, 0.1)` for subtle colored sections
- **Container queries**: The system uses CSS container queries, not viewport media queries
- **Naming**: Name classes by purpose (`callout-blue`, `quote-accent`) not by appearance (`big-blue-box`)

### WRONG vs RIGHT

**Wrong** — inline style accumulation:
```html
<div style="background: rgba(0,120,255,0.1); padding: 20px; border-radius: 12px; margin-bottom: 20px;">
  <div style="display: flex; align-items: center; gap: 15px; margin-bottom: 10px;">
    <span style="font-size: 2em; color: #007ACC; font-weight: bold;">Title</span>
  </div>
  <p style="font-size: 1.1em; opacity: 0.8;">Content here</p>
</div>
```

**Right** — CSS class:
```css
/* next-slide.css */
.feature-card {
  background: rgba(0, 120, 255, 0.1);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
}
.feature-card h3 {
  font-size: 2em;
  color: var(--vscode-textLink-foreground);
  font-weight: bold;
}
```
```html
<div class="feature-card">

### Title

Content here

</div>
```

## Content Sizing Guidelines

| Element | Guideline |
|---------|-----------|
| Bullet points | 3–7 words each, concise |
| Bullets per column | 5–7 maximum |
| Heading h1 | Use `.title-slide` for large (6vw); default for normal |
| Images | CSS constrains automatically; use `max-height` for explicit sizing |
| Code blocks | Use `<pre>` with `font-size: 0.9rem` for code-heavy slides |
| Viewport fit | Design for half-screen width (typical preview pane) |

## Image Handling

### Path Conventions

Image paths in markdown resolve **relative to the markdown file's location**, not the workspace root.

**Before writing any image reference**, determine `{image-path}` — the correct relative path from the slide file to the image directory:

1. Find where images actually live (list directories)
2. Compute the relative traversal from the slide's directory to the image directory
3. Use that path consistently for all image references in the slide

Examples of how the same image directory produces different paths depending on slide location:

| Slide file | Image directory | `{image-path}` |
|------------|----------------|----------------|
| `slides/topic.md` | `images/` (workspace root) | `../images` |
| `slides/deep/topic.md` | `images/` (workspace root) | `../../images` |
| `slides/topic.md` | `slides/images/` | `images` |
| `topic.md` (workspace root) | `images/` (workspace root) | `images` |

The pattern examples throughout this skill use `{image-path}` as a placeholder — always substitute the computed relative path.

Always include `alt` text on images.

### Image Sizing

CSS automatically constrains images (`max-width: 100%`). For explicit sizing:
- Use a CSS class (preferred): `.hero-image { max-height: 50vh; }`
- Use inline `style="max-height: 200px;"` for one-offs
- Use `.image-focus` for a single centered prominent image
- Use `.gallery` for grid layouts of multiple images

## Deck Configuration

### slides.edn Format

```clojure
{:slides ["slides/welcome.md"
          "slides/topic-one.md"
          "slides/examples/demo.md"]}
```

- Vector order is **presentation order** (authoritative)
- Paths are relative to workspace root
- Must be valid EDN (no trailing commas)
- All paths must reference `.md` files

When adding a new slide, insert it at the appropriate position in the vector.

## Speaker Notes

The system auto-discovers notes via naming convention:

| Slide | Notes file |
|-------|-----------|
| `slides/topic.md` | `slides/topic-notes.md` |
| `slides/examples/demo.md` | `slides/examples/demo-notes.md` |

Notes are created automatically by the system's `prepare!` function. The agent typically doesn't create notes files manually, but should be aware they exist and follow this naming pattern.

Notes have a structured format with sections for story narrators, slide narration authors, and the narration script itself.

## Verification Checklist

Before finishing a slide:

- [ ] All referenced images exist at the specified paths
- [ ] If using `.row`/`.col-N` (legacy), column widths add up to ≤ 12
- [ ] If using `cols-*`, `.col` div count matches the column count
- [ ] Blank lines surround markdown content inside HTML divs
- [ ] CSS classes used in the slide exist in `next-slide.css`
- [ ] Content fits the viewport (or uses `.scroller` for overflow)
- [ ] `slides.edn` updated with the new slide path in correct position
- [ ] `slides.edn` is valid EDN
- [ ] Inline styles are justified (not a repeated pattern that should be a CSS class)
- [ ] Images have `alt` text

## Troubleshooting Layouts: DOM Inspection Process

VS Code's markdown preview transforms your HTML through markdown-it parsing and its own scroll-sync injection. The actual DOM may differ from what you wrote. When a layout doesn't behave as expected:

### The Process

1. **Create a test slide** — Isolate the problematic pattern in a minimal markdown file (e.g., `slides/dom-experiments.md`)
2. **Preview it** — Open the markdown preview in VS Code
3. **Open DevTools** — `Help > Toggle Developer Tools` (or `Cmd+Shift+I`)
4. **Inspect the DOM** — In the Elements tab, find your slide container and examine:
   - Are your elements direct children of the grid/flex container, or wrapped in injected `<div>` elements?
   - Does VS Code add attributes like `data-line` or classes like `code-line`?
   - Are there empty marker `<div>` elements interspersed with your content?
5. **Copy the rendered HTML** — Right-click the container element → "Copy element" to get the exact DOM
6. **Share with the agent** — Paste the copied DOM so the agent can write CSS that targets the actual structure

### Known VS Code Preview Behaviors

- **Empty marker divs**: VS Code injects empty `<div class="code-line" data-line="N">` divs between HTML blocks for scroll synchronization. These are pure markers with no content.
- **Content element decoration**: Content elements (h1, p, ul, etc.) get the `.code-line` class and `data-line` attribute, but remain their original element type — they're not wrapper divs.
- **`.col` divs stay clean**: VS Code does NOT inject `.code-line` onto `.col` divs, so they work as direct grid children.
- **The `cols-*` fix**: `div.code-line:not(.col) { display: none }` hides the empty marker divs without hiding content elements (which are not `<div>`s).
- **Markdown block separation**: Blank lines between HTML and markdown cause markdown-it to treat them as separate blocks.

### Why This Matters

CSS Grid is sensitive to DOM structure — extra wrapper elements break `grid-column` placement. Flexbox (used by the `.row`/`.column` system) is more forgiving. When using Grid-based layouts (`cols-*`), always verify the actual DOM if the layout doesn't match expectations.

## Common Pitfalls

| Mistake | Fix |
|---------|-----|
| Markdown not rendering inside HTML | Add blank lines before and after markdown content within `<div>` tags |
| Images not showing | Check path is relative to the markdown file's location |
| Content overflows viewport | Reduce content or wrap in `<div class="scroller">` |
| Slides look different in light/dark | Use `var(--vscode-*)` theme variables instead of hardcoded colors |
| Re-implementing auto behaviors | Check if `.social-links`, `.scroller` fade, or image constraining already handles it |
| Inline style sprawl | Extract to a CSS class in `next-slide.css` |
| Column layout breaks on narrow | This is intentional — container queries stack columns at ≤ 600px |
| Using `.row`/`.column`/`.col-N` for new slides | Prefer `cols-*` grid system — simpler (2 nesting levels vs 3) and handles VS Code DOM injection automatically |
| Using `display: contents` on `[data-line]` elements | NEVER — it destroys the element's own box, collapsing headings and paragraphs. The `cols-*` system uses `display: none` on empty marker divs instead |