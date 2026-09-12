import { useCallback, useEffect, useMemo, useState } from 'react';
import { readDirectory, saveDirectory } from '../model/directoryStore';
import { resolvePluginRoot } from '../model/pluginDirectory';
import type { PluginRoot } from '../model/pluginDirectory';
import { WorkspaceContext } from '../model/workspaceContext';

type DirectoryPickerWindow = Window & {
  showDirectoryPicker?: () => Promise<FileSystemDirectoryHandle>;
};

function describe(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback;
}

/** Owns the single plugin directory and restores the last one on startup. */
export function WorkspaceProvider({ children }: { children: React.ReactNode }) {
  const [root, setRoot] = useState<PluginRoot>();
  const [restored, setRestored] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    void readDirectory()
      .then(async (handle) => (handle ? resolvePluginRoot(handle) : undefined))
      .catch(() => undefined) // The stored folder moved or lost permission; the user picks it again.
      .then((resolved) => {
        if (cancelled) return;
        if (resolved) setRoot(resolved);
        setRestored(true);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const openFolder = useCallback(async () => {
    setError('');
    try {
      const picker = (window as DirectoryPickerWindow).showDirectoryPicker;
      if (!picker) throw new Error('当前浏览器不支持直接访问文件夹，请使用 Chrome 或 Edge。');
      const handle = await picker();
      // Validate before storing so a wrong folder is not restored on the next visit.
      const resolved = await resolvePluginRoot(handle);
      await saveDirectory(handle);
      setRoot(resolved);
    } catch (cause) {
      if ((cause as DOMException).name !== 'AbortError')
        setError(describe(cause, '无法打开插件目录'));
    }
  }, []);

  const clearError = useCallback(() => setError(''), []);
  const value = useMemo(
    () => ({ root, restored, error, openFolder, clearError }),
    [root, restored, error, openFolder, clearError],
  );
  return <WorkspaceContext value={value}>{children}</WorkspaceContext>;
}
