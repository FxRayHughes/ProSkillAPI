/** Release object URLs after the browser has accepted the download. */
export function downloadText(
  filename: string,
  content: string,
  mimeType = 'application/yaml;charset=utf-8',
): void {
  const url = URL.createObjectURL(new Blob([content], { type: mimeType }));
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename.replace(/[<>:"/\\|?*]/g, '_');
  document.body.append(anchor);
  anchor.click();
  anchor.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}
