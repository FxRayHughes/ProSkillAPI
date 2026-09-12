import { createContext, use } from 'react';
import type { PluginRoot } from './pluginDirectory';

export interface WorkspaceValue {
  root?: PluginRoot;
  /** False until the stored handle has been checked, so pages do not flash an empty state. */
  restored: boolean;
  error: string;
  openFolder: () => Promise<void>;
  clearError: () => void;
}

export const WorkspaceContext = createContext<WorkspaceValue | undefined>(undefined);

/** One directory for the whole app: every page reads the same plugin folder. */
export function useWorkspace(): WorkspaceValue {
  const value = use(WorkspaceContext);
  if (!value) throw new Error('useWorkspace 必须在 WorkspaceProvider 内使用');
  return value;
}
