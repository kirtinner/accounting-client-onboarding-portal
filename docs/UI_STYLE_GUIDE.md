# UI Style Guide

This guide defines the visual rules for the React frontend. The application should feel like a modern enterprise tool: calm, clear, consistent, and optimized for repeated operational use.

## Color Palette

- Page background: `#F3F7FC`
- Surface background: `#FFFFFF`
- Muted surface: `#F5F9FF`
- Primary blue: `#2563EB`
- Primary hover: `#1D4ED8`
- Light blue fill: `#DBEAFE`
- Table header blue: `#E8F1FF`
- Borders: `#D7E3F3`
- Strong borders: `#CBD5E1`
- Text: `#142033`
- Muted text: `#52647A`
- Danger text: `#B42318`
- Danger background: `#FFF4F2`

Use blue as the main UI accent. Avoid gray-heavy panels when a soft blue-tinted surface would better support hierarchy.

## Buttons

- Primary buttons use the primary blue background with white text.
- Secondary buttons use a white background with a blue-gray border.
- Danger buttons use red text on a very light red background.
- Buttons should have consistent height, padding, font weight, and border radius.
- Disabled buttons should remain visible but clearly inactive.

## Tables

- Table headers must be visually distinct from data rows.
- Header labels are uppercase, `font-weight: 600`, and use a light blue background.
- Header cells are slightly taller than body cells.
- Header rows use a clear bottom border to separate headings from data.
- Data rows stay simple, with soft borders and a restrained hover state.

## Selected Rows

- Selected rows use a visible light blue background.
- The selected row has a `3px` primary-blue left accent.
- Selected text may be slightly stronger than regular row text.
- Hovering a selected row must not remove the selected accent or make selection ambiguous.

## Status Badges

- Status badges use compact rounded rectangles with clear text contrast.
- Prefer soft backgrounds with matching borders.
- Status colors should communicate state without dominating the table.

## Modals

- Modals use the shared surface, border, shadow, and border radius.
- Modal headers use a subtle blue-tinted surface and a bottom border.
- Forms use consistent spacing and field sizing.
- Modal actions align to the right and use the same button variants as page actions.
- Confirmation dialogs must use the React modal pattern, not native browser dialogs.

## Sidebar

- The sidebar uses white or very light blue surfaces.
- Active navigation uses a light blue background and primary blue text.
- Future sections remain visible but disabled-looking.

## Messages

- Error messages are shown with subtle red styling.
- Success messages for routine create, update, send, or cancel operations are not displayed.
- Successful operations should refresh the relevant data and preserve selection when possible.

## General UX Principles

- Keep screens compact and useful; avoid decorative panels or redundant text.
- Use the page title for context instead of repeating titles inside table cards.
- Keep spacing consistent across page layout, tables, buttons, and modals.
- Prefer clear state changes over transient success notifications.
